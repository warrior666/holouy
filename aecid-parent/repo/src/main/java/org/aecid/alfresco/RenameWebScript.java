package org.aecid.alfresco;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;


public class RenameWebScript extends DeclarativeWebScript {

	private Repository repository;

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	protected Map<String, Object> executeImpl(WebScriptRequest req,
			Status status, Cache cache) {

		NodeRef folder;

		// extract node arguments from URI
		String node = req.getParameter("node");

		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();

		String folderPath = templateArgs.get("folderpath");
		String nodePath = "workspace/SpacesStore/" + folderPath;

		folder = repository.findNodeRef("path", nodePath.split("/"));


		// validate that folder has been found
		if (folder == null) {
			throw new WebScriptException(Status.STATUS_NOT_FOUND, "Folder "
					+ folderPath + " not found");
		}

		// construct model for response template to render
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("folder", folder);
		return model;
	}
}                  