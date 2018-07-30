package org.aecid.alfresco;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

public class RenameActionExecuter extends ActionExecuterAbstractBase {

	private final static Logger log = Logger.getLogger(RenameActionExecuter.class);
	
	// public static final String PARAM_DESTINATION_FOLDER = "destination-folder";

	private NodeService nodeService;
	private FileFolderService fileFolderService;
	private DictionaryService dictionaryService;
	
	// @Override
	// protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
	// paramList.add(
	// new ParameterDefinitionImpl(PARAM_DESTINATION_FOLDER,
	// DataTypeDefinition.NODE_REF,
	// true,
	// getParamDisplayLabel(PARAM_DESTINATION_FOLDER)));
	// }

	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see org.alfresco.repo.action.executer.ActionExecuter#execute(org.alfresco.repo.ref.NodeRef,
	 *      org.alfresco.repo.ref.NodeRef)
	 */
	public void executeImpl(Action ruleAction, NodeRef nodeRef) {

		log.info("executeImpl Action renamer by rule: " + ruleAction.getActionDefinitionName());
		log.info("Noderef: " + nodeRef.getId());
		
		QName qtype = nodeService.getType(nodeRef);
		TypeDefinition typeDef = dictionaryService.getType(qtype);
				
		typeDef.getTitle();
		// Fetch all the properties
		Map<QName, Serializable> props = nodeService.getProperties(nodeRef);

		// Fetch a few common properties
		String name = (String) props.get(ContentModel.PROP_NAME);
		String title = (String) props.get(ContentModel.PROP_TITLE);

		String description = "Name: " + name + " / Title: " + title;
		
		log.info("Property description: " + description);
		
		nodeService.setProperty(nodeRef, ContentModel.PROP_DESCRIPTION, description);

	}

	public void setDictionaryService(DictionaryService service) {
		dictionaryService = service;
	}

	public void setNodeService(NodeService service) {
		nodeService = service;
	}

	public void setFileFolderService(FileFolderService service) {
		fileFolderService = service;
	}

}
