package org.aecid.alfresco;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

public class PropertiesPropagationActionExecuter extends ActionExecuterAbstractBase {

	private final static Logger log = Logger.getLogger(PropertiesPropagationActionExecuter.class);

	private static final String TYPE_SEPARATOR = "-";
	private static final String PROJECT_TAG_SEPARATOR = "_";
	private static final String STRING_EMPTY = "";
	
	private static final String PROJECT_TAG_PROPERTY = "projectTag";
	private static final String BUDGET_YEAR_PROPERTY = "budgetYear";
	private static final String PROGRAM_PROPERTY = "program";
	private static final String COMPONENT_PROPERTY = "component";
	private static final String PROJECT_TITLE_PROPERTY = "projectTitle";
	
	private final static String AECID_MODEL_1_0_URI = "http://aecid.org/model/aecid/1.0";
	
	private NodeService nodeService;
	private FileFolderService fileFolderService;
	private DictionaryService dictionaryService;
	
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {

	}

	/**
	 * @see org.alfresco.repo.action.executer.ActionExecuter#execute(org.alfresco.repo.ref.NodeRef,
	 *      org.alfresco.repo.ref.NodeRef)
	 */
	public void executeImpl(Action ruleAction, NodeRef nodeRef) {

		log.info("executeImpl action propagate properties by rule: " + ruleAction.getActionDefinitionName());
		log.info("Noderef: " + nodeRef.getId());
		
		if (isProjectFolder(nodeRef)) {
			propagateProperties(nodeRef);
			
		} else {
			obtainProperties(nodeRef);
		}
	}

	public void obtainProperties(NodeRef nodeRef) {
		
		ChildAssociationRef assoc = nodeService.getPrimaryParent(nodeRef);
		NodeRef parent = assoc.getParentRef();
		
		
		log.info("obtainProperties: " + parent.getId());

		copyProperties(parent, nodeRef);
		
	}
	
	private String getNodeName(NodeRef nodeRef) {
		// Fetch all the properties
		Map<QName, Serializable> props = nodeService.getProperties(nodeRef);

		// Fetch a few common properties
		String name = (String) props.get(ContentModel.PROP_NAME);
		
		return name;
	}
	
	private void copyProperties(NodeRef fromNode, NodeRef toNode) {
		
		Map<QName, Serializable> props = nodeService.getProperties(fromNode);
	
		copyProperty(props, PROJECT_TAG_PROPERTY, toNode);
		copyProperty(props, BUDGET_YEAR_PROPERTY, toNode);
		copyProperty(props, PROGRAM_PROPERTY, toNode);
		copyProperty(props, COMPONENT_PROPERTY, toNode);
		copyProperty(props, PROJECT_TITLE_PROPERTY, toNode);

		copyProperty(props, "country", toNode);
		copyProperty(props, "fileNumber", toNode);
		copyProperty(props, "startDate", toNode);
		copyProperty(props, "endingDate", toNode);
		copyProperty(props, "justificationDate", toNode);
		copyProperty(props, "extensionDate", toNode);

		copyProperty(props, "modality", toNode);
		copyProperty(props, "program", toNode);
		
		copyProperty(props, "location", toNode);
		copyProperty(props, "beneficiary", toNode);
		copyProperty(props, "counterpart", toNode);
		copyProperty(props, "colaborator", toNode);
				
		// En caso de contener, se copian las categorias
		if (props.containsKey(ContentModel.PROP_CATEGORIES)) {
			Object o = props.get(ContentModel.PROP_CATEGORIES);
			
			log.info("copyProperty categories to node: " + toNode.getId());
			nodeService.setProperty(toNode, ContentModel.PROP_CATEGORIES, (Serializable) o);
		}
		
		// En caso de existir se copian las Palabras Claves
		if (props.containsKey(ContentModel.PROP_TAGS)) {
			Object o = props.get(ContentModel.PROP_TAGS);
			
			log.info("copyProperty tags to node: " + toNode.getId());
			nodeService.setProperty(toNode, ContentModel.PROP_TAGS, (Serializable) o);
		}
	}
	
	
	private void copyProperty(Map<QName, Serializable> props, String propKey, NodeRef toNode) {
		
		QName qname = QName.createQName(AECID_MODEL_1_0_URI, propKey);
		
		if (props.containsKey(qname)) {
			Object o = props.get(qname);
			if (o != null) {
				log.info("copyProperty: " + propKey + " to node: " + toNode.getId());
				nodeService.setProperty(toNode, qname, (Serializable) o);
			}
		}	
	}

	public void propagateProperties(NodeRef nodeRef) {
		log.info("propagateProperties: " + nodeRef.getId());
		List<ChildAssociationRef> childs = nodeService.getChildAssocs(nodeRef);
		propagateProperties(nodeRef, childs);
	}
	
	
	private void propagateProperties(NodeRef nodeRef, List<ChildAssociationRef> childs) {
		
		if (childs == null) {
			return;
		}
		
		log.info("propagateProperties: " + nodeRef.getId() + " childs size: " + childs.size());
		
		if (childs.isEmpty()) {
			return;
		}
		
		for (ChildAssociationRef child : childs) {

			if (!child.getTypeQName().equals(ContentModel.ASSOC_CONTAINS)) {
				log.info("childRef not a folder contains.");
				continue;
			}
			
			NodeRef childRef = child.getChildRef();
			
			if (childRef == null) {
				log.info("childRef null.");
				continue;
			}

			if (isProjectable(childRef)) {
				log.info("copyProperties: " + nodeRef.getId() + " to node: " + childRef.getId());
				copyProperties(nodeRef, childRef);
			}
			
			// Es una carpeta
			if (isProjectFolder(childRef)) {
				List<ChildAssociationRef> list = nodeService.getChildAssocs(childRef);
				log.info("looking childs of : " + getNodeName(childRef));
				propagateProperties(nodeRef, list);
			}
		}
	}

	private boolean isProjectFolder(NodeRef nodeRef) {
		QName qtype = nodeService.getType(nodeRef);
	
		if (qtype.getLocalName().equals("folderProject")) {
			return true;
		}
		
		return false;
	}
	
	private boolean isProjectable(NodeRef nodeRef) {

		QName qtype = nodeService.getType(nodeRef);

		log.info("isProjectable : " + getNodeName(nodeRef) + " type: " + qtype.getLocalName());
		
		switch (qtype.getLocalName()) {
		case "folderProject": return true;
		case "contentProject": return true;
		case "dscProy": return true;
		case "diagnosis": return true;
		case "convenio": return false;
		case "memo": return false;
		case "publication": return true;
		case "trackingSheet": return true;
		case "subResolution": return true;
		case "contentGral": return false;
		case "acta" : return false;
		case "product" : return true;
		case "contract" : return false;
		case "subExtension" : return true;
		case "evaluation" : return true;
		case "report" : return true;
		case "formulation" : return true;
		case "generalPlan" : return true;
		case "anualPlan" : return true;
		case "subJustification" : return true;
		case "finalReport" : return true;
		case "subActos" : return true;
		case "protocol" : return true;
		case "subModification" : return true;
		case "budgetSheet" : return true;
		case "referenceTerm" : return true;
		case "normative" : return false;
		case "subAdenda" : return true;
		case "preIdentFile" : return true;
		case "subsidy" : return true;
		case "subAcept" : return true;
		case "agreement" : return false;
		case "countryReport" : return false;
		}
		
		return false;
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
