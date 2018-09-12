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
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

public class RenameActionExecuter extends ActionExecuterAbstractBase {

	private final static Logger log = Logger.getLogger(RenameActionExecuter.class);

	private static final String TYPE_SEPARATOR = "-";
	private static final String PROJECT_TAG_SEPARATOR = "_";
	private static final String STRING_EMPTY = "";
	private static final String PROJECT_TAG_PROPERTY = "projectTag";
	private static final String BUDGET_YEAR_PROPERTY = "budgetYear";
	private static final String PROGRAM_PROPERTY = "program";
	private static final String COMPONENT_PROPERTY = "component";
	
	private final static String AECID_MODEL_1_0_URI = "http://aecid.org/model/aecid/1.0";
	
	private NodeService nodeService;
	private FileFolderService fileFolderService;
	private DictionaryService dictionaryService;
	
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see org.alfresco.repo.action.executer.ActionExecuter#execute(org.alfresco.repo.ref.NodeRef,
	 *      org.alfresco.repo.ref.NodeRef)
	 */
	public void executeImpl(Action ruleAction, NodeRef nodeRef) {

		log.info("executeImpl action renamer by rule v2: " + ruleAction.getActionDefinitionName());
		log.info("Noderef: " + nodeRef.getId());
		
		String type = getTypeTitle(nodeRef);
	
		// Fetch all the properties
		Map<QName, Serializable> props = nodeService.getProperties(nodeRef);

		// Fetch a few common properties
		String name = (String) props.get(ContentModel.PROP_NAME);
		String title = (String) props.get(ContentModel.PROP_TITLE);

		log.info("renamer name: " + name + " title: " + title);
		
		if (title == null || title.equals(STRING_EMPTY)) {
			title = name;
			nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, title);
		}
		
		String projectTag = getProperty(props, PROJECT_TAG_PROPERTY);
		String budgetYear =  getProperty(props, BUDGET_YEAR_PROPERTY);
		String program =  getProperty(props, PROGRAM_PROPERTY);
		String component =  getProperty(props, COMPONENT_PROPERTY);
		
		
		StringWriter sw = new StringWriter();

		if (!projectTag.equals(STRING_EMPTY)) {
			sw.write(projectTag + PROJECT_TAG_SEPARATOR);
		}
		
		if (!name.contains(type)) {
			sw.write(type);
		}
		
		if (!budgetYear.equals(STRING_EMPTY)) {
			sw.write(" (" + budgetYear + ")");
			log.info("renamer add budgetYear: " + budgetYear);
			
		} else if (!program.equals(STRING_EMPTY)) {
			sw.write(TYPE_SEPARATOR + program);
			
			log.info("renamer add program: " + program);
			
			if (!component.equals(STRING_EMPTY)) {
				sw.write(TYPE_SEPARATOR + component);
			}
		}
				
		log.info("Rename: " + sw.toString());
		
		nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, sw.toString());
	}

	
	private String getTypeTitle(NodeRef nodeRef) {
		QName qtype = nodeService.getType(nodeRef);
		TypeDefinition typeDef = dictionaryService.getType(qtype);
		
		String typeTitle = typeDef.getTitle();
		
		String abrev = "";
		
		switch (qtype.getLocalName()) {
		case "content": abrev = "CM"; break;
		case "contentProject": abrev = "CONPROY"; break;
		case "dscProy": abrev = "DSCP"; break;
		case "diagnosis": abrev = "DIAG"; break;
		case "convenio": abrev = "CONV"; break;
		case "memo": abrev = "MDE"; break;
		case "publication": abrev = "PUB"; break;
		case "trackingSheet": abrev = "FST"; break;
		case "subResolution": abrev = "SUBRES"; break;
		case "contentGral": abrev = "CONGRAL"; break;
		case "acta" : abrev = "ACTA"; break;
		case "product" : abrev = "PROD"; break;
		case "contract" : abrev = "CON"; break;
		case "subExtension" : abrev = "SUBEXT"; break;
		case "evaluation" : abrev = "INFEV"; break;
		case "report" : abrev = "INF"; break;
		case "formulation" : abrev = "DFORM"; break;
		case "generalPlan" : abrev = "POG"; break;
		case "anualPlan" : abrev = "POA"; break;
		case "subJustification" : abrev = "SUBJUS"; break;
		case "finalReport" : abrev = "INFF"; break;
		case "subActos" : abrev = "SUBACT"; break;
		case "protocol" : abrev = "PROT"; break;
		case "subModification" : abrev = "SUBMOD"; break;
		case "budgetSheet" : abrev = "CEJE"; break;
		case "referenceTerm" : abrev = "TDR"; break;
		case "normative" : abrev = "NORM"; break;
		case "subAdenda" : abrev = "SUBADD"; break;
		case "preIdentFile" : abrev = "PREID"; break;
		case "subsidy" : abrev = "SUB"; break;
		case "subAcept" : abrev = "SUBACE"; break;
		case "agreement" : abrev = "AEC"; break;
		case "countryReport" : abrev = "NPAIS"; break;
		
		default:
			abrev = typeTitle;
		}
		
		log.info("getTypeTitle - Looking for abrev: " + abrev + " for qname: " + qtype.getLocalName());
		
		return abrev;
	}
	
	private String getProperty(Map<QName, Serializable> props, String key) {
		QName qname = QName.createQName(AECID_MODEL_1_0_URI, key);
		
		if (props.containsKey(qname)) {
			Object o = props.get(qname);
			if (o instanceof Integer) {
				return String.valueOf(o);
				
			} else if (o instanceof Long) {
				return String.valueOf(o);
				
			} else {
				return (String) o;
			}
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
