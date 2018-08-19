package org.huy.tools.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.huy.tools.loader.FileLoader;
import org.huy.tools.loader.FolderLoader;
import org.huy.tools.util.GsonHelper;

import com.alfresco.client.AbstractClient;
import com.alfresco.client.RestClient;
import com.alfresco.client.Version;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;


public class LoaderController extends AbstractController<LoaderController> {

	protected static FileLoader fileLoader;
	protected static FolderLoader folderLoader;

	public static final String DEFAULT_ENDPOINT = "http://localhost:8080/alfresco/";

	public static final String DEFAULT_USER = "admin";

	public static final String DEFAULT_PASSWORD = "admin";

    public static final String USERID_SYSTEM = "System";

    public static final String USERID_ADMIN = "admin";

    public static final String USER_DISPLAY_ADMIN = "Administrator";

    public static final ArrayList<String> DEFAULT_FOLDER_ASPECTS = new ArrayList<String>(3)
    {
        {
            add("cm:titled");
            add("cm:auditable");
            add("app:uifacets");
        }
    };

    public static final ArrayList<String> DEFAULT_FILE_ASPECTS = new ArrayList<String>(3)
    {
        {
            add("cm:titled");
            add("cm:auditable");
            add("app:uifacets");
            add("cm:versionable");
            add("cm:author");
        }
    };
    		
    		
	protected static final Object LOCK = new Object();

	protected static LoaderController mInstance;

	
	public static FileLoader getFileLoader() {
		return fileLoader;
	}

	public static FolderLoader getFolderLoader() {
		return folderLoader;
	}


	public static void process(File root) throws IOException {
		File[] listOfFiles = root.listFiles();
		
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				System.out.println("File " + listOfFiles[i].getName());
				LoaderController.getFileLoader().process(listOfFiles[i]);
			} else if (listOfFiles[i].isDirectory()) {
				System.out.println("Directory " + listOfFiles[i].getName());
				LoaderController.getFolderLoader().process(listOfFiles[i]);
			}
		}
	}
	
	
	// ///////////////////////////////////////////////////////////////////////////
	// CONSTRUCTOR
	// ///////////////////////////////////////////////////////////////////////////
	public static void initInstance() {
		mInstance = prepareClient(DEFAULT_ENDPOINT, DEFAULT_USER, DEFAULT_PASSWORD);
	}

	public static LoaderController getInstance() {
		return mInstance;
	}
	
	private LoaderController(RestClient restClient, OkHttpClient okHttpClient) {
		super(restClient, okHttpClient);

		fileLoader = new FileLoader(this);
		folderLoader = new FolderLoader(this);

	}


    // ///////////////////////////////////////////////////////////////////////////
    // ALFRESCO CLIENT UTILS
    // ///////////////////////////////////////////////////////////////////////////
    public static LoaderController prepareDefaultClient()
    {
    	LoaderController client = new LoaderController.Builder().connect("http://cmis.alfresco.com/", "admin", "admin")
                .httpLogging(HttpLoggingInterceptor.Level.BODY).build();
    	return client;
    }

    public static LoaderController prepareClient(String endpoint, String username, String password)
    {
        return new LoaderController.Builder().connect(endpoint, username, password)
                .httpLogging(HttpLoggingInterceptor.Level.BODY).build();
    }
    

	// ///////////////////////////////////////////////////////////////////////////
	// BUILDER
	// ///////////////////////////////////////////////////////////////////////////
	public static class Builder extends AbstractClient.Builder<LoaderController> {

		@Override
		public String getUSerAgent() {
			return "Alfresco-ECM-Client/" + Version.SDK;
		}

		@Override
		public LoaderController create(RestClient restClient, OkHttpClient okHttpClient) {
			return new LoaderController(new RestClient(endpoint, retrofit, username), okHttpClient);
		}

		@Override
		public GsonBuilder getDefaultGsonBuilder() {
			return GsonHelper.getDefaultGsonBuilder();
		}

		// BUILD
		// ///////////////////////////////////////////////////////////////////////////
		@Override
		public LoaderController build() {
			// Create Client
			mInstance = super.build();
			return mInstance;
		}
	}


}
