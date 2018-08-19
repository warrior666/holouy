package org.huy.tools.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.huy.tools.loader.FileLoader;
import org.huy.tools.loader.FolderLoader;
import org.huy.tools.util.GsonHelper;

import com.alfresco.client.AbstractClient;
import com.alfresco.client.AlfrescoClient;
import com.alfresco.client.RestClient;
import com.alfresco.client.Version;
import com.alfresco.client.api.authentication.AuthenticationAPI;
import com.alfresco.client.api.core.ActivitiesAPI;
import com.alfresco.client.api.core.CommentsAPI;
import com.alfresco.client.api.core.FavoritesAPI;
import com.alfresco.client.api.core.NodesAPI;
import com.alfresco.client.api.core.PeopleAPI;
import com.alfresco.client.api.core.QueriesAPI;
import com.alfresco.client.api.core.RatingsAPI;
import com.alfresco.client.api.core.RenditionsAPI;
import com.alfresco.client.api.core.SharedLinksAPI;
import com.alfresco.client.api.core.SitesAPI;
import com.alfresco.client.api.core.TagsAPI;
import com.alfresco.client.api.core.TrashcanAPI;
import com.alfresco.client.api.core.VersionAPI;
import com.alfresco.client.api.discovery.DiscoveryAPI;
import com.alfresco.client.api.search.SearchAPI;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;


public class AbstractController<T> extends AbstractClient<T> {

	protected static FileLoader fileLoader;
	protected static FolderLoader folderLoader;

	public static final String DEFAULT_ENDPOINT = "http://localhost:8080/alfresco/";

	public static final String DEFAULT_USER = "admin";

	public static final String DEFAULT_PASSWORD = "admin";

    public static final String USERID_SYSTEM = "System";

    public static final String USERID_ADMIN = "admin";

    public static final String USER_DISPLAY_ADMIN = "Administrator";
	
    		
	protected static final Object LOCK = new Object();

	protected static AbstractController mInstance;

	protected ActivitiesAPI activitiesAPI;
	protected CommentsAPI commentsAPI;
	protected NodesAPI nodesAPI;
	protected PeopleAPI peopleAPI;
	protected QueriesAPI queriesAPI;
	protected RatingsAPI ratingsAPI;
	protected RenditionsAPI renditionsAPI;
	protected SharedLinksAPI sharedLinksAPI;
	protected SitesAPI sitesAPI;
	protected TagsAPI tagsAPI;
	protected TrashcanAPI trashcanAPI;
	protected FavoritesAPI favoritesAPI;
	protected DiscoveryAPI discoveryAPI;
	protected AuthenticationAPI authenticationAPI;
	protected SearchAPI searchAPI;
	// protected GroupsAPI groupsAPI;
	protected VersionAPI versionAPI;

		
	protected AbstractController(RestClient restClient, OkHttpClient okHttpClient) {
		super(restClient, okHttpClient);
	}


    // ///////////////////////////////////////////////////////////////////////////
    // ALFRESCO CLIENT UTILS
    // ///////////////////////////////////////////////////////////////////////////
    public static AbstractController prepareDefaultClient()
    {
    	AbstractController client = new AbstractController.Builder().connect("http://cmis.alfresco.com/", "admin", "admin")
                .httpLogging(HttpLoggingInterceptor.Level.BODY).build();
    	return client;
    }

    public static AbstractController prepareClient(String endpoint, String username, String password)
    {
        return new AbstractController.Builder().connect(endpoint, username, password)
                .httpLogging(HttpLoggingInterceptor.Level.BODY).build();
    }

    // TODO Design it better.
    public AlfrescoClient prepareClientTicket(String endpoint, String ticket)
    {
        return new AlfrescoClient.Builder().connectWithTicket(endpoint, ticket)
                .httpLogging(HttpLoggingInterceptor.Level.BODY).build();
    }
    
    
	// ///////////////////////////////////////////////////////////////////////////
	// API REGISTRY
	// ///////////////////////////////////////////////////////////////////////////
	public ActivitiesAPI getActivitiesAPI() {
		if (activitiesAPI == null) {
			activitiesAPI = getAPI(ActivitiesAPI.class);
		}
		return activitiesAPI;
	}

	public CommentsAPI getCommentsAPI() {
		if (commentsAPI == null) {
			commentsAPI = getAPI(CommentsAPI.class);
		}
		return commentsAPI;
	}

	public FavoritesAPI getFavoritesAPI() {
		if (favoritesAPI == null) {
			favoritesAPI = getAPI(FavoritesAPI.class);
		}
		return favoritesAPI;
	}

	public NodesAPI getNodesAPI() {
		if (nodesAPI == null) {
			nodesAPI = getAPI(NodesAPI.class);
		}
		return nodesAPI;
	}

	public PeopleAPI getPeopleAPI() {
		if (peopleAPI == null) {
			peopleAPI = getAPI(PeopleAPI.class);
		}
		return peopleAPI;
	}

	public RatingsAPI getRatingsAPI() {
		if (ratingsAPI == null) {
			ratingsAPI = getAPI(RatingsAPI.class);
		}
		return ratingsAPI;
	}

	public SitesAPI getSitesAPI() {
		if (sitesAPI == null) {
			sitesAPI = getAPI(SitesAPI.class);
		}
		return sitesAPI;
	}

	public TagsAPI getTagsAPI() {
		if (tagsAPI == null) {
			tagsAPI = getAPI(TagsAPI.class);
		}
		return tagsAPI;
	}

	public QueriesAPI getQueriesAPI() {
		if (queriesAPI == null) {
			queriesAPI = getAPI(QueriesAPI.class);
		}
		return queriesAPI;
	}

	public RenditionsAPI getRenditionsAPI() {
		if (renditionsAPI == null) {
			renditionsAPI = getAPI(RenditionsAPI.class);
		}
		return renditionsAPI;
	}

	public SharedLinksAPI getSharedLinksAPI() {
		if (sharedLinksAPI == null) {
			sharedLinksAPI = getAPI(SharedLinksAPI.class);
		}
		return sharedLinksAPI;
	}

	public TrashcanAPI getTrashcanAPI() {
		if (trashcanAPI == null) {
			trashcanAPI = getAPI(TrashcanAPI.class);
		}
		return trashcanAPI;
	}

	public DiscoveryAPI getDiscoveryAPI() {
		if (discoveryAPI == null) {
			discoveryAPI = getAPI(DiscoveryAPI.class);
		}
		return discoveryAPI;
	}

	public AuthenticationAPI getAuthenticationAPI() {
		if (authenticationAPI == null) {
			authenticationAPI = getAPI(AuthenticationAPI.class);
		}
		return authenticationAPI;
	}

	public SearchAPI getSearchAPI() {
		if (searchAPI == null) {
			searchAPI = getAPI(SearchAPI.class);
		}
		return searchAPI;
	}

	// Not implemented
	/*
	 * public GroupsAPI getGroupsAPI() { if (groupsAPI == null) { groupsAPI =
	 * getAPI(GroupsAPI.class); } return groupsAPI; }
	 */

	public VersionAPI getVersionAPI() {
		if (versionAPI == null) {
			versionAPI = getAPI(VersionAPI.class);
		}
		return versionAPI;
	}

	// ///////////////////////////////////////////////////////////////////////////
	// AUTHENTICATION
	// ///////////////////////////////////////////////////////////////////////////
	public void setTicket(String ticket) {

		OkHttpClient.Builder builder = getOkHttpClient().newBuilder();

		// Remove old interceptor
		int index = 0;
		for (int i = 0; i < builder.interceptors().size(); i++) {
			if (builder.interceptors().get(i) instanceof BasicAuthInterceptor
					|| builder.interceptors().get(i) instanceof TicketInterceptor) {
				index = i;
			}
		}

		builder.interceptors().remove(index);
		builder.interceptors().add(new TicketInterceptor(ticket));
		okHttpClient = builder.build();
	}

	// ///////////////////////////////////////////////////////////////////////////
	// BUILDER
	// ///////////////////////////////////////////////////////////////////////////
	public static class Builder extends AbstractClient.Builder<AbstractController> {

		@Override
		public String getUSerAgent() {
			return "Alfresco-ECM-Client/" + Version.SDK;
		}

		@Override
		public AbstractController create(RestClient restClient, OkHttpClient okHttpClient) {
			return new AbstractController(new RestClient(endpoint, retrofit, username), okHttpClient);
		}

		@Override
		public GsonBuilder getDefaultGsonBuilder() {
			return GsonHelper.getDefaultGsonBuilder();
		}

		// BUILD
		// ///////////////////////////////////////////////////////////////////////////
		public AbstractController build() {
			// Create Client
			mInstance = super.build();
			return mInstance;
		}
	}


}
