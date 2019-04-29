package org.huy.tools.model;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;

import org.huy.tools.controller.LoaderController;

import com.alfresco.client.api.common.representation.ResultPaging;
import com.alfresco.client.api.core.NodesAPI;
import com.alfresco.client.api.core.model.body.NodeBodyCreate;
import com.alfresco.client.api.core.model.body.NodeBodyUpdate;
import com.alfresco.client.api.core.model.representation.NodeRepresentation;
import com.google.gson.internal.LinkedTreeMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;

public class NodeDTO {

	NodesAPI nodeService;

	ResultPaging<NodeRepresentation> nodes;
	NodeRepresentation node;

	public NodeDTO() {
		nodeService = LoaderController.getInstance().getAPI(NodesAPI.class);
	}

	public void selectFolder(String folder) throws IOException {

		Response<ResultPaging<NodeRepresentation>> response = nodeService.listNodeChildrenCall(NodesAPI.FOLDER_MY)
				.execute();
		nodes = response.body();
		// response.isSuccessful();

	}

	public NodeRepresentation find(String name) {

		for (NodeRepresentation nr : nodes.getList()) {

			if (name.equals(nr.getName())) {
				return nr;
			}
		}
		return null;
	}

	public void delete() throws IOException {
		nodeService.deleteNodeCall(node.getId()).execute();
	}

	public void createFolderNode(String name) throws IOException {
		// CREATE
		NodeBodyCreate request = new NodeBodyCreate(name, "cm:folder");
		Response<NodeRepresentation> response = nodeService.createNodeCall(NodesAPI.FOLDER_ROOT, request).execute();

		node = response.body();

	}

	public void createFileNode(String folder, String name, File file) throws IOException {

		// Create Body
		String mimeType = URLConnection.guessContentTypeFromName(file.getName());

		RequestBody requestBody = RequestBody.create(MediaType.parse(mimeType), file);
		MultipartBody.Builder multipartBuilder = new MultipartBody.Builder();
		multipartBuilder.addFormDataPart(name, file.getName(), requestBody);
		RequestBody fileRequestBody = multipartBuilder.build();

		// Let's Create
		Response<NodeRepresentation> response = nodeService.createNodeCall(folder, fileRequestBody).execute();
		node = response.body();
	}

	public void rename(String newName) throws IOException {

		// RENAME
		NodeBodyUpdate renameRequest = new NodeBodyUpdate(newName);
		Response<NodeRepresentation> updatedResponse = nodeService.updateNodeCall(node.getId(), renameRequest)
				.execute();

		Response<NodeRepresentation> response = nodeService.getNodeCall(node.getId()).execute();
		node = response.body();
	}

	public void setTitle(String title) throws IOException {

		// EDIT PROPERTIES
		LinkedTreeMap<String, Object> properties = new LinkedTreeMap<>();
		properties.put("cm:title", title);
		NodeBodyUpdate editRequest = new NodeBodyUpdate(node.getName(), null, properties, null);
		Response<NodeRepresentation> response = nodeService.updateNodeCall(node.getId(), editRequest).execute();
		node = response.body();
	}

}