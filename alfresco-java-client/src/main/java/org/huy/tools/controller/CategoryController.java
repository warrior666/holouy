package org.huy.tools.controller;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.huy.tools.category.CategoryImportTool;
import org.huy.tools.model.CategoryDTO;
import org.huy.tools.util.GsonHelper;

import com.alfresco.client.AbstractClient;
import com.alfresco.client.RestClient;
import com.alfresco.client.Version;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class CategoryController extends AbstractController<CategoryController> {

	private final static Logger log = Logger.getLogger(CategoryController.class);

	protected static CategoryImportTool tool;

	protected static CategoryController mInstance;

	public static CategoryImportTool getCategoryImportTool() {
		return tool;
	}

	public static void process(String filename) throws IOException {

		log.info("Importing categories from " + filename);

		// read categories
		List<CategoryDTO> categories = tool.readCategoriesFromFile(filename);

		// Obtengo las categorias ya existentes
		List<CategoryDTO> loaded = CategoryDTO.loadCategories(categories.get(0).getGroup());
		
		// Filtro las categorias aun no guardadas
		List<CategoryDTO> filtered = CategoryDTO.filter(categories, loaded);
		
		// Grabo las categorias faltantes
		CategoryDTO.storeCategories(filtered);

		// Obtengo toda las categorias
		List<CategoryDTO> stored = CategoryDTO.loadCategories(categories.get(0).getGroup());
		
		// write categories
		tool.writeCategoriesToFile(stored, filename + ".out");
		
		log.info("Category import completed.");

	}

	// ///////////////////////////////////////////////////////////////////////////
	// CONSTRUCTOR
	// ///////////////////////////////////////////////////////////////////////////
	public static void initInstance() {
		mInstance = prepareClient(DEFAULT_ENDPOINT, DEFAULT_USER, DEFAULT_PASSWORD);
	}

	public static CategoryController getInstance() {
		return mInstance;
	}

	private CategoryController(RestClient restClient, OkHttpClient okHttpClient) {
		super(restClient, okHttpClient);

		tool = new CategoryImportTool(this);

	}

	// ///////////////////////////////////////////////////////////////////////////
	// ALFRESCO CLIENT UTILS
	// ///////////////////////////////////////////////////////////////////////////
	public static CategoryController prepareDefaultClient() {
		CategoryController client = new CategoryController.Builder()
				.connect("http://cmis.alfresco.com/", "admin", "admin").httpLogging(HttpLoggingInterceptor.Level.BODY)
				.build();
		return client;
	}

	public static CategoryController prepareClient(String endpoint, String username, String password) {
		return new CategoryController.Builder().connect(endpoint, username, password)
				.httpLogging(HttpLoggingInterceptor.Level.BODY).build();
	}

	// ///////////////////////////////////////////////////////////////////////////
	// BUILDER
	// ///////////////////////////////////////////////////////////////////////////
	public static class Builder extends AbstractClient.Builder<CategoryController> {

		@Override
		public String getUSerAgent() {
			return "Alfresco-ECM-Client/" + Version.SDK;
		}

		@Override
		public CategoryController create(RestClient restClient, OkHttpClient okHttpClient) {
			return new CategoryController(new RestClient(endpoint, retrofit, username), okHttpClient);
		}

		@Override
		public GsonBuilder getDefaultGsonBuilder() {
			return GsonHelper.getDefaultGsonBuilder();
		}

		// BUILD
		// ///////////////////////////////////////////////////////////////////////////
		public CategoryController build() {
			// Create Client
			mInstance = super.build();
			return mInstance;
		}
	}

}
