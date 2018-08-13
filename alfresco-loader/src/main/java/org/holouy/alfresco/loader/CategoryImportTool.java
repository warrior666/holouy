package org.holouy.alfresco.loader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.alfresco.webservice.authentication.AuthenticationFault;
import org.alfresco.webservice.repository.QueryResult;
import org.alfresco.webservice.repository.RepositoryFault;
import org.alfresco.webservice.types.CML;
import org.alfresco.webservice.types.CMLCreate;
import org.alfresco.webservice.types.NamedValue;
import org.alfresco.webservice.types.ParentReference;
import org.alfresco.webservice.types.Query;
import org.alfresco.webservice.types.Reference;
import org.alfresco.webservice.types.ResultSet;
import org.alfresco.webservice.types.ResultSetRow;
import org.alfresco.webservice.types.Store;
import org.alfresco.webservice.util.AuthenticationUtils;
import org.alfresco.webservice.util.Constants;
import org.alfresco.webservice.util.ISO9075;
import org.alfresco.webservice.util.Utils;
import org.alfresco.webservice.util.WebServiceFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <pre>
 * Create a Category hierarchy in Alfresco from a plain text file.
 * 
 * This Class uses the Alfresco Web Services Client to connect to an Alfresco CMS.
 * 
 * <b>Usage:</b> java com.nkics.alfrescox.tool.CategoryImportTool &lt;filename&gt;
 * <ul>filename - the text file containing the categories to be created.</ul>
 * 
 * The text file should contain one category path (hierarchy) per line.
 * Each category path is formatted like this: /cm:Parent/cm:SubCategory/cm:SubSubCategory
 * Parent categories that do not exist will be created automatically.
 * 
 * This class logs messages using the Common Logging package.
 * 
 * Many of the methods in this class were defined in a reusable manner.  In other words, You don't have to execute this class
 * from the command line.  Any of the public methods can be used to leverage all or part of the functionality provided by this tool.
 * 
 * For example
 * <ul>
 * <li>To Create a Category: Simply create an instance of CategoryImportTool, and call createCategory(String path).
 * <li>To Create multiple Categories from any source: Simply create an instance of CategoryImportTool, and call createCategories(Collection categories).
 * </pre>
 * @author swick April 22, 2009
 */
public class CategoryImportTool
{
   public static final String ALFRESCO_USERNAME_PROPERTY = "repository.username";
   public static final String ALFRESCO_PASSWORD_PROPERTY = "repository.password";
   public static final String ALFRESCO_WEBSERVICECLIENT_PROPERTIES = "/alfresco/webserviceclient.properties";
   private static final String ROOT_CATEGORY = "/cm:generalclassifiable";
   private final String SUBCATEGORIES = "subcategories"; // the propertyname of subcategories
   private final String SUBCATEGORIES_Q = Constants.createQNameString(Constants.NAMESPACE_CONTENT_MODEL, SUBCATEGORIES);
   private final String CATEGORY = "category"; // the propertyname of subcategories
   private final String CATEGORY_Q = Constants.createQNameString(Constants.NAMESPACE_CONTENT_MODEL, CATEGORY);
   private final Store STORE = new Store(Constants.WORKSPACE_STORE, "SpacesStore");
   /** Maps Parent Category Paths to Reference objects. Caches Parent Category lookups. **/
   private Map<String,Reference> ROOT_REFERENCE_CACHE = new HashMap<String,Reference>();
   private final Log logger = LogFactory.getLog(getClass());

   /**
    * Invoke the CategoryImportTool from the command line.
    * 
    * @param args Command line arguments.  arg[0] should be the filename containing the categories to import.
    */
   public static void main(String[] args) throws Exception
   {
      if (args.length == 0)
         printUsage();

      CategoryImportTool migration = new CategoryImportTool();
      migration.runApp(args[0]);
   }

   /**
    * This method creates an Alfresco Web Service session {@link #setUp()}, reads the categories to 
    * import from the filename provided {@link #readCategoriesFromFile(String)}, creates each category,
    * and ends the Alfresco web service session.  
    * @param filename is the name of a file on the local filesystem to import.
    * @throws AuthenticationFault if there is a problem creating the Alfresco web service session.  (username/password is invalid, or alfresco/webserviceclient.properties cannot be found or cannot connect to the URL specified)
    * @throws RemoteException - There was a problem querying for or creating a category.
    * @throws IOException - There was a problem reading the import file, the file doesn't exist, or you don't have permission to read the file.
    */
   public void runApp(String filename) throws IOException
   {
      logger.info("Importing categories from "+filename);
      setUp();
      Collection<String> categories = readCategoriesFromFile(filename);
      createCategories(categories);
      tearDown();
      logger.info("Category import completed.");
   }

   /**
    * Read categories from a file into a Collection.
    * This method simply adds each line from the file into the Collection.
    * Blank lines and comments (lines starting with a # character) are ignored (they are not included in the Collection.
    * @param fileName is the full path to a file to open for reading.
    * @return a new Collection of Strings, one per line in the file.
    * @throws IOException if there is a problem reading the filename specified.
    */
   public Collection<String> readCategoriesFromFile(String fileName) throws IOException
   {
      BufferedReader in = new BufferedReader(new FileReader(fileName));
      Vector<String> categories = new Vector<String>();
      String str = null;
      while ((str = in.readLine()) != null)
      {
         if(str.trim().length()==0)
            continue; // skip blank lines
         if(str.trim().startsWith("#"))
            continue; // skip comments starting with the # character
         
         categories.add(str);
      }
      in.close();
      return categories;
   }

   /**
    * This method loops over the Collection provided and invokes {@link #createCategory(String)}
    * @param categories is a Collection of Strings, where each String is a category path to be created.  example: "/cm:CatA/cm:Cat1/cm:CatI"
    * @throws RemoteException is thrown from {@link #createCategory(String)} if there is a problem creating a category in Alfresco.
    */
   public void createCategories(Collection<String> categories) throws RemoteException
   {
      for (String category : categories)
         createCategory(category);
   }

   /**
    * <pre>
    * Create a Category in Alfresco.  This method will automatically create any parent categories if they do not exist.
    * If the Category already exist, it will be skipped without throwing an exception.
    * 
    * The following messages are written using the Common Logging package
    * </pre>
    * <ul>
    * <li><em>category already exists: {path}</em> # Logged at the INFO level if the category already exists.
    * <li><em>creating category: {path}</em> # Logged at the INFO level if the category is being created (it does not exist).
    * </ul>
    * @param path is a Category Path (hierarchy) like "/cm:CatA/cm:Cat1/cm:CatI"
    * @throws RemoteException from {@link #createCategory(String, ParentReference)} if Alfresco reports a problem creating the category.
    */
   public void createCategory(String path) throws RemoteException
   {
      // Recursively create parent categories, if necessary.
      Reference rootRef = null;
      String parentCategoryPath = getParentCategoryPath(path);
      while((rootRef=getRootReference(parentCategoryPath))==null)
      {
         createCategory(parentCategoryPath);
      }

      // Make sure the category doesn't already exist in Alfresco.
      if(getCategory(path)!=null)
      {
         logger.info("category already exists: "+path);
         return;
      }
      
      // Create the category.
      logger.info("creating category:" + path);
      String childCategoryName = getChildCategoryName(path);
      ParentReference parentRef = new ParentReference(STORE, rootRef.getUuid(), null, SUBCATEGORIES_Q, Constants.createQNameString(Constants.NAMESPACE_CONTENT_MODEL, childCategoryName));
      createCategory(childCategoryName, parentRef);
   }

   /**
    * Get the Parent categories from the category path provided.
    * 
    * @param path a Category path like "/cm:CatA/cm:Cat1/cm:CatI"
    * @return all text up to the last / character, or null if there is no / character in the path provided.  ie: "/cm:CatA/cm:Cat1"
    */
   protected String getParentCategoryPath(String path)
   {
      if(path.lastIndexOf("/")<=0)
         return null;
      
      return path.substring(0, path.lastIndexOf("/"));
   }

   /**
    * Get the child category name from the category path provided.
    * 
    * @param path a Category path like "/cm:CatA/cm:Cat1/cm:CatI"
    * @return all text from the last "/cm:" character sequence to the end of the path.  ie: "CatI"
    */
   protected String getChildCategoryName(String path)
   {
      return path.substring(path.lastIndexOf("/cm:")+4); // get the last category and strip off the "/cm:".
   }

   /**
    * Actually create the Category described by the parentRef and categoryName provided
    * @param categoryName is just the Name of the category to be created, without a namespace.  ie: "CatI"
    * @param parentRef is a reference to the parent category.  ie: "/cm:CatA/cm:Cat1"
    * @throws RemoteException 
    */
   protected void createCategory(String categoryName, ParentReference parentRef) throws RemoteException
   {
      NamedValue[] properties = new NamedValue[] { Utils.createNamedValue(Constants.PROP_NAME, categoryName) };

      CMLCreate create = new CMLCreate("1", parentRef, null, null, null, CATEGORY_Q, properties);
      CML cml = new CML();
      cml.setCreate(new CMLCreate[] { create });

      WebServiceFactory.getRepositoryService().update(cml);
   }

   /**
    * Get the Parent Category specified.  Root Categories are cached to improve performance and reduce load on the Alfresco server.
    * @param parentCategoryPath is a path relative to /cm:generalclassifiable (ROOT_CATEGORY).  Can be null, in which case ROOT_CATEGORY is returned.
    * @return a Reference to the Root Category
    * @throws RemoteException if {@link #getCategory(String)} throws this exception.
    * @throws RepositoryFault if {@link #getCategory(String)} throws this exception.
    * @see getCategory(String)
    */
   private Reference getRootReference(String parentCategoryPath) throws RepositoryFault, RemoteException
   {
      // Return Cached Reference, if available.
      if(ROOT_REFERENCE_CACHE.containsKey(parentCategoryPath))
         return ROOT_REFERENCE_CACHE.get(parentCategoryPath);

      // Get the Category from Alfresco, cache it, and return it.
      Reference r = getCategory(parentCategoryPath);
      if(r!=null)
         ROOT_REFERENCE_CACHE.put(parentCategoryPath, r); // Add Reference to cache.
      return r;
   }

   /**
    * Get a Category from Alfresco.
    * @param categoryPath is a path relative to /cm:generalclassifiable (ROOT_CATEGORY).  Can be null, in which case /cm:generalclassifiable is returned.
    * @return a Reference to the Category requested, or null if the category doesn't exist.
    * @throws RemoteException 
    * @throws RepositoryFault 
    */
   public Reference getCategory(String categoryPath) throws RepositoryFault, RemoteException
   {
      String luceneQueryString = "PATH:\""+ROOT_CATEGORY+(categoryPath==null?"":encodeCategoryPath(categoryPath))+"\"";
      Query query = new Query(Constants.QUERY_LANG_LUCENE, luceneQueryString);

      QueryResult result = WebServiceFactory.getRepositoryService().query(STORE, query, true);
      ResultSet rs = result.getResultSet();
      if(rs.getTotalRowCount()==0)
         return null;
      ResultSetRow[] rows = rs.getRows();
      String uuid = rows[0].getNode().getId();
      return new Reference(STORE, uuid, null);
   }

   /**
    * Apply ISO9075 encoding to the Category Path provided.
    * 
    * @param categoryPath is a String like "/cm:Sports/cm:Water Polo"
    * @return the categoryPath provided with ISO9075 encoding applied to each category in the path, like "/cm:Sports/cm:Water_x0020_Polo".
    */
   public static final String encodeCategoryPath(String categoryPath)
   {
      categoryPath = categoryPath.substring(4); // Strip off the leading '/cm:'
      String[] categories = categoryPath.split("/cm:"); // Split the category path by the remaining '/cm:' strings.
      StringBuffer encodedCategoryPath = new StringBuffer();
      for(String category : categories)
      {
         encodedCategoryPath.append("/cm:");
         encodedCategoryPath.append(ISO9075.encode(category));
      }
      return encodedCategoryPath.toString();
   }

   /**
    * Creates an Alfresco web service session.  Called by {@link #runApp(String)}.
    * <p>
    * By default the username and password are "admin".  The default values can be overridden by adding the {@link #ALFRESCO_USERNAME_PROPERTY}
    * and {@link #ALFRESCO_PASSWORD_PROPERTY} to the {@link #ALFRESCO_WEBSERVICECLIENT_PROPERTIES} file, or by
    * specifying these properties on the Java command line with the "-D" command line argument.  System properties will take precedence
    * over {@link #ALFRESCO_WEBSERVICECLIENT_PROPERTIES}.
    * @see #ALFRESCO_USERNAME_PROPERTY
    * @see #ALFRESCO_PASSWORD_PROPERTY
    * @see #ALFRESCO_WEBSERVICECLIENT_PROPERTIES
    * @throws AuthenticationFault if there is a problem logging into the Alfresco web service with the username and password provided.
    * @throws IOException if there is a problem reading ALFRESCO_WEBSERVICECLIENT_PROPERTIES from the classloader.
    */
   public void setUp() throws AuthenticationFault, IOException
   {
      // Set the Default values.
      String username = "admin";
      String password = "admin";
      
      // Attempt to read the username and password from the Alfresco web service client properties file.
      InputStream is = getClass().getResourceAsStream(ALFRESCO_WEBSERVICECLIENT_PROPERTIES);
      if(is!=null)
      {
         Properties properties = new Properties();
         properties.load(is);
         username = getUsername(properties, username);
         password = getPassword(properties, password);
      }
      
      // Attempt to read the username and password from the System properties.
      username = getUsername(System.getProperties(), username);
      password = getPassword(System.getProperties(), password);
      
      logger.info("creating Alfresco web service client session with username '"+username+"' and password '"+password+"'.");
      
      AuthenticationUtils.startSession(username, password);
   }

   protected String getUsername(Properties p, String originalValue)
   {
      if(p.containsKey(ALFRESCO_USERNAME_PROPERTY))
         return p.getProperty(ALFRESCO_USERNAME_PROPERTY);
      return originalValue;
   }
   
   protected String getPassword(Properties p, String originalValue)
   {
      if(p.containsKey(ALFRESCO_PASSWORD_PROPERTY))
         return p.getProperty(ALFRESCO_PASSWORD_PROPERTY);
      return originalValue;
   }

   /**
    * Ends the Alfresco web service session.  Called by {@link #runApp(String)}.
    */
   public void tearDown()
   {
      AuthenticationUtils.endSession();
   }

   protected static void printUsage()
   {
      System.out.println("java "+CategoryImportTool.class.getName()+" <filename>");
   }
}