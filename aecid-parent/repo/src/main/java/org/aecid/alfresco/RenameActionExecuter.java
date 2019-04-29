package org.aecid.alfresco;

import java.io.Serializable;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
	private static final String PROJECT_TITLE_PROPERTY = "projectTitle";

	private static final String BUDGET_YEAR_PROPERTY = "budgetYear";
	private static final String PROGRAM_PROPERTY = "program";
	private static final String COMPONENT_PROPERTY = "component";
	private static final String CONTENT_DATE_PROPERTY = "contentDate";
	
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

		String typeAbrev = getTypeAbrev(nodeRef);
		String typeTitle = getTypeTitle(nodeRef);
	
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
		String projectTitle = getProperty(props, PROJECT_TITLE_PROPERTY);
		String budgetYear =  getProperty(props, BUDGET_YEAR_PROPERTY);
		String program =  getProperty(props, PROGRAM_PROPERTY);
		String component =  getProperty(props, COMPONENT_PROPERTY);
		String contentDate = getProperty(props, CONTENT_DATE_PROPERTY);
		
		StringWriter swName = new StringWriter();
		StringWriter swTitle = new StringWriter();
		
		if (!projectTag.equals(STRING_EMPTY)) {
			swName.write(projectTag + PROJECT_TAG_SEPARATOR);
		}

		if (!projectTitle.equals(STRING_EMPTY)) {
			swTitle.write(projectTitle + PROJECT_TAG_SEPARATOR);
		}
		
		// En todos los casos el type es parte del nombre
		swName.write(typeAbrev);
		swTitle.write(typeTitle);
		
		if (!budgetYear.equals(STRING_EMPTY)) {
			swName.write(" (" + budgetYear + ")");
			swTitle.write(" (" + budgetYear + ")");
			log.info("renamer add budgetYear: " + budgetYear);
			
		} else if (!program.equals(STRING_EMPTY)) {
			swName.write(TYPE_SEPARATOR + program);
			log.info("renamer add program: " + program);
			
			if (!component.equals(STRING_EMPTY)) {
				swName.write(TYPE_SEPARATOR + component);
			}
		} else if (!contentDate.equals(STRING_EMPTY)) {
			
			swName.write(" (" + contentDate + ")");
			swTitle.write(" (" + contentDate + ")");
			log.info("renamer add contentDate: " + contentDate);
		}
				
		log.info("Rename: " + swName.toString());
		
		nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, swName.toString());
		nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, swTitle.toString());
	}

	
	private String getTypeAbrev(NodeRef nodeRef) {
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
		case "pressNote" : abrev = "PREN"; break;
		case "brochure" : abrev = "FOLL"; break;
		
		default:
			abrev = typeTitle;
		}
		
		log.info("getTypeAbrev - Looking for abrev: " + abrev + " for qname: " + qtype.getLocalName());
		
		return abrev;
	}
	
	
	private String getTypeTitle(NodeRef nodeRef) {
		QName qtype = nodeService.getType(nodeRef);
		TypeDefinition typeDef = dictionaryService.getType(qtype);
		
		String typeTitle = typeDef.getTitle();
		
		String abrev = "";
		
		switch (qtype.getLocalName()) {
		case "content": abrev = "Contenido sin clasificar"; break;
		case "contentProject": abrev = "Contenido de proyecto"; break;
		case "dscProy": abrev = "Descripcion de Proyecto"; break;
		case "diagnosis": abrev = "Diagnostico"; break;
		case "convenio": abrev = "Convenio"; break;
		case "memo": abrev = "Memorandum de Entendimiento"; break;
		case "publication": abrev = "Publicacion"; break;
		case "trackingSheet": abrev = "Ficha de Seguimiento Trimestral"; break;
		case "subResolution": abrev = "Subvencion Resolucion"; break;
		case "contentGral": abrev = "Contenido general"; break;
		case "acta" : abrev = "Acta"; break;
		case "product" : abrev = "Producto"; break;
		case "contract" : abrev = "Contrato"; break;
		case "subExtension" : abrev = "Subvencion Extension"; break;
		case "evaluation" : abrev = "Informe Evaluacion"; break;
		case "report" : abrev = "Informe"; break;
		case "formulation" : abrev = "Documento de Formulacion"; break;
		case "generalPlan" : abrev = "Plan Operativo General"; break;
		case "anualPlan" : abrev = "Plan Operativo Anual"; break;
		case "subJustification" : abrev = "Subvencion Justificacion"; break;
		case "finalReport" : abrev = "Informe Final"; break;
		case "subActos" : abrev = "Subvencion Acto"; break;
		case "protocol" : abrev = "Protocolo"; break;
		case "subModification" : abrev = "Subvencion Modificacion"; break;
		case "budgetSheet" : abrev = "Cuadro de Ejecucion Presupuestal"; break;
		case "referenceTerm" : abrev = "Terminos de Referencia"; break;
		case "normative" : abrev = "Normativa"; break;
		case "subAdenda" : abrev = "Subvencion Addenda"; break;
		case "preIdentFile" : abrev = "Ficha pre Identificacion"; break;
		case "subsidy" : abrev = "Subvencion"; break;
		case "subAcept" : abrev = "Subvencion Aceptacion"; break;
		case "agreement" : abrev = "Acuerdo Entidad Colaboradora"; break;
		case "countryReport" : abrev = "Nota Pais"; break;
		case "pressNote" : abrev = "Comunicado de Prensa"; break;
		case "brochure" : abrev = "Folleto"; break;
		
		default:
			abrev = typeTitle;
		}
		
		log.info("getTypeTitle - Looking for title: " + abrev + " for qname: " + qtype.getLocalName());
		
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
				
			} else if (o instanceof Date) {
				
				try {
					SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy");
				
					Date date = (Date) o;

					return outputFormat.format(date);
					
				} catch (Exception e) {
					e.printStackTrace();
					log.error("Error formato de fecha", e);
				}
				
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
