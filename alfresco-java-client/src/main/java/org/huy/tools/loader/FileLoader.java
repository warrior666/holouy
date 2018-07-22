package org.huy.tools.loader;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.huy.tools.controller.AlfrescoController;
import org.huy.tools.model.NodeDTO;
import org.huy.tools.util.FilenameDocument;

import com.alfresco.client.AlfrescoClient;
import com.alfresco.client.api.search.body.QueryBody;
import com.alfresco.client.api.search.body.RequestQuery;
import com.alfresco.client.api.search.model.ResultNodeRepresentation;
import com.alfresco.client.api.search.model.ResultSetRepresentation;

import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;

public class FileLoader {


	private AlfrescoController controller;


	public FileLoader(AlfrescoController client) {
		controller = client;
	}


	public void process(File file) throws IOException {

		FilenameDocument filename = new FilenameDocument(file.getName());

		// searching
		ResultNodeRepresentation rNode = searchByTitle(filename.getTitle());
		
		if (rNode == null) {
			System.out.println("NUEVO NODO");
			NodeDTO dto = new NodeDTO();
			dto.createFileNode("test", filename.getTitle(), file);

		} 
	}

	
	public ResultNodeRepresentation searchByTitle(String title) throws IOException {
		String queryStr = "(name:\"*" + title + "*\" OR title:\"*" + title + "*\")";

		RequestQuery query = new RequestQuery().query(queryStr);
		QueryBody body = new QueryBody().query(query);

		// Request
		Response<ResultSetRepresentation<ResultNodeRepresentation>> response = controller.getSearchAPI().searchCall(body)
				.execute();

		ResultSetRepresentation<ResultNodeRepresentation> resultSet = response.body();
		List<ResultNodeRepresentation> results = resultSet.getList();

		return results.get(0);
	}



}
