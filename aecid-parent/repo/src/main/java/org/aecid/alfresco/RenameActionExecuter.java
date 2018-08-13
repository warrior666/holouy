package org.aecid.alfresco;

import java.io.Serializable;
import java.io.StringWriter;
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
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

public class RenameActionExecuter extends ActionExecuterAbstractBase {

	private static final String TYPE_SEPARATOR = "-";
	private static final String PROJECT_TAG_SEPARATOR = "_";
	private static final String STRING_EMPTY = "";
	private static final String PROJECT_TAG_PROPERTY = "projectTag";
	private final static String AECID_MODEL_1_0_URI = "http://aecid.org/model/aecid/1.0";
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
		
		String type = getTypeTitle(nodeRef);
	
		// Fetch all the properties
		Map<QName, Serializable> props = nodeService.getProperties(nodeRef);

		// Fetch a few common properties
		String name = (String) props.get(ContentModel.PROP_NAME);
		String title = (String) props.get(ContentModel.PROP_TITLE);

		if (title.equals(STRING_EMPTY)) {
			title = name;
			nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, title);
		}
		
		String projectTag = getProjectTag(props);
		StringWriter sw = new StringWriter();

		if (!projectTag.equals(STRING_EMPTY)) {
			sw.write(projectTag + PROJECT_TAG_SEPARATOR);
		}
		
		if (!name.contains(type)) {
			sw.write(type + TYPE_SEPARATOR);
		}
		
		sw.write(title);
		
		log.info("Property name: " + sw.toString());
		
		nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, sw.toString());

	}

	private String getTypeTitle(NodeRef nodeRef) {
		QName qtype = nodeService.getType(nodeRef);
		TypeDefinition typeDef = dictionaryService.getType(qtype);
				
		String typeTitle = typeDef.getTitle();
		return typeTitle;
	}
	
	private String getProjectTag(Map<QName, Serializable> props) {
		QName qname = QName.createQName(AECID_MODEL_1_0_URI, PROJECT_TAG_PROPERTY);
		
		if (props.containsKey(qname)) {
			return (String) props.get(qname);
		}
		
		return STRING_EMPTY;
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