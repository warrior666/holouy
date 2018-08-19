package org.huy.tools.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.util.ISO9075;
import org.apache.log4j.Logger;
import org.huy.tools.controller.CategoryController;

import com.alfresco.client.api.core.model.body.NodeBodyCreate;
import com.alfresco.client.api.core.model.representation.NodeRepresentation;
import com.alfresco.client.api.search.body.QueryBody;
import com.alfresco.client.api.search.body.RequestQuery;
import com.alfresco.client.api.search.body.RequestQuery.LanguageEnum;
import com.alfresco.client.api.search.model.ResultNodeRepresentation;
import com.alfresco.client.api.search.model.ResultSetRepresentation;

import retrofit2.Call;
import retrofit2.Response;

public class CategoryDTO {

	private static final String EMPTY_GROUP = "";

	private final static Logger log = Logger.getLogger(CategoryDTO.class);
	
	private static final String DELIMITER = ".";
	private static final String CM = "/cm:";
	private static final String ROOT_CATEGORY = "/cm:categoryRoot/cm:generalclassifiable";
	private static final String DEFAULT_NODE_TYPE = "cm:category";
	
	public String id;
	public String path;
	public String route;
	public ResultNodeRepresentation node;
	
	public String group;
	public String name;
	public String nodeType;
	

	public String getNodeType() {
		return nodeType;
	}

	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}

	public CategoryDTO() {
		nodeType = DEFAULT_NODE_TYPE;
	}
	
	public CategoryDTO(String str) {
		this();

		String[] aux = str.split("\\.");
		
		if (aux.length == 1) {
			name = str;
		} else {
			group = aux[0];
			// TODO: extenderlo a mas de un nivel.
			name = aux[aux.length - 1];
		}
		
		path = ROOT_CATEGORY + (group==null?EMPTY_GROUP:CM + ISO9075.encode(group))+ CM + ISO9075.encode(name);
		
		route = str;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}

	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public ResultNodeRepresentation getNode() {
		return node;
	}
	public void setNode(ResultNodeRepresentation node) {
		this.node = node;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public boolean existIn(List<CategoryDTO> categories) {
		for (CategoryDTO cat : categories) {
			if (cat.getName().equals(getName()) && cat.getGroup().equals(getGroup())) {
				return true;
			}
		}
		return false;
	}
	
	public String toString() {
		return group + DELIMITER + name;
	}
	
	public static List<CategoryDTO> loadCategories(String group) {

		List<CategoryDTO> aux = new ArrayList<>();
		
		try {

			RequestQuery rq = new RequestQuery();
			rq.setLanguage(LanguageEnum.LUCENE);
			rq.setQuery("+PATH:\"/cm:categoryRoot/cm:generalclassifiable/cm:" + group + "/*\"");
			QueryBody body = new QueryBody();
			body.setQuery(rq);

			Call<ResultSetRepresentation<ResultNodeRepresentation>> call1 = CategoryController.getInstance().getSearchAPI().searchCall(body);

			Response<ResultSetRepresentation<ResultNodeRepresentation>> response = call1.execute();

			log.info("loadCategories read size: " + response.body().getList().size());

			for (ResultNodeRepresentation rNode : response.body().getList()) {
				CategoryDTO cat = CategoryDTO.build(group, rNode);
				aux.add(cat);
			}

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return aux;
	}

	
	public static final CategoryDTO build(String path) {
		
		System.out.println("Building category path: " + path);
		
		CategoryDTO cat = new CategoryDTO();
		List<String> aux = new ArrayList<>();
		
		cat.setNodeType(CategoryDTO.DEFAULT_NODE_TYPE);

		String[] categories = path.split(CM); // Split the category path by the remaining '/cm:' strings.

		for (String category : categories) {
			aux.add(ISO9075.decode(category));
		}
		
		cat.setGroup(aux.get(0));
		cat.setName(aux.get(aux.size() - 1));

		//String joined = String.join(DELIMITER, aux);

		cat.setPath(path);
		
		return cat;
	}
	
	public static final CategoryDTO build(String group, NodeRepresentation rNode) {
		
		System.out.println("Building category group " + group + ", name: " + rNode.getName());
		
		CategoryDTO cat = new CategoryDTO();
		List<String> aux = new ArrayList<>();
		
		cat.setId(rNode.getId());
		cat.setNodeType(rNode.getNodeType());
		cat.setName(rNode.getName());
		cat.setGroup(group);
		//rNode = rNode.substring(ROOT_CATEGORY.length()); // Strip off the ROOT_CATEGORY
		//String[] categories = rNode.split(CM); // Split the category path by the remaining '/cm:' strings.

		//for (String category : categories) {
		//	aux.add(ISO9075.decode(category));
		//}
		
		//cat.setGroup(aux.get(0));
		//cat.setName(aux.get(aux.size() - 1));

		String joined = String.join(DELIMITER, aux);
		
		//cat.setRoute(joined);
		cat.setRoute(group + DELIMITER + rNode.getName());
		//cat.setPath(rNode);
		
		return cat;
	}
	
	public static final List<CategoryDTO> filter(List<CategoryDTO> categories, List<CategoryDTO> filter) {
		
		List<CategoryDTO> aux = new ArrayList<>();
		
		for (CategoryDTO cat : categories) {
			if (!cat.existIn(filter))
				aux.add(cat);
		}
		
		return aux;
	}

	public NodeRepresentation store() throws IOException {
		
		return store(this);
	}
		
	public static void storeCategories(List<CategoryDTO> categories) throws IOException {
		for (CategoryDTO category : categories)
			category.store();
	}
	
	public static NodeRepresentation store(CategoryDTO cat) throws IOException {

			String categoryPath = cat.getPath();
			return store(categoryPath);
	}
	
	public static NodeRepresentation store(String path) throws IOException {

			String parentPath = getParentCategoryPath(path);
			String categoryName = getChildCategoryName(path);
			
			NodeRepresentation nodeParent = findByPath(parentPath);
			CategoryDTO parent = null;
			
			// while ((rootRef = getRootReference(parentCategoryPath)) == null) {
			if (nodeParent != null) {
				parent = build(parentPath);
			} else {
				parent = build(EMPTY_GROUP, nodeParent);
				parent.setPath(parentPath);
				nodeParent = store(parent);
			}

			ResultNodeRepresentation node = findByPath(path);
			
			// Make sure the category doesn't already exist in Alfresco.
			if (node != null) {
				log.info("category already exists: " + path);
				return node;
			}

			// Create the category.
			log.info("creating category:" + path);

			return store(categoryName, CategoryDTO.DEFAULT_NODE_TYPE, nodeParent);
		}
		

	public static String getParentCategoryPath(String path) {
		if (path.lastIndexOf("/") <= 0)
			return null;

		return path.substring(0, path.lastIndexOf("/"));
	}
	
    protected static String getChildCategoryName(String path) {
        return path.substring(path.lastIndexOf("/cm:")+4); // get the last category and strip off the "/cm:".
     }
	
    
	public static ResultNodeRepresentation findByPath(String path) {

		try {

			RequestQuery rq = new RequestQuery();
			rq.setLanguage(LanguageEnum.LUCENE);
			rq.setQuery("+PATH:\"" + path + "\"");
			QueryBody body = new QueryBody();
			body.setQuery(rq);

			Call<ResultSetRepresentation<ResultNodeRepresentation>> call = CategoryController.getInstance().getSearchAPI().searchCall(body);

			Response<ResultSetRepresentation<ResultNodeRepresentation>> response = call.execute();

			log.info("loadCategory read size: " + response.body().getList().size());
			
			if (response.body().getList() == null || response.body().getList().isEmpty()) {
				return null;
			}
			
			return response.body().getList().get(0);

		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		return null;
	}
	

	public static NodeRepresentation store(String name, String nodeType, NodeRepresentation parent) throws IOException {
		

			NodeBodyCreate body = new NodeBodyCreate(name, nodeType);
			
			// Let's Create
			Response<NodeRepresentation> response = CategoryController.getInstance().getNodesAPI().createNodeCall(parent.getId(), body).execute();
			NodeRepresentation node = response.body();
		return node;
	}
}
