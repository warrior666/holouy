package org.aecid.alfresco;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.calendar.CalendarEntry;
import org.alfresco.service.cmr.calendar.CalendarService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

public class ScheduledJobExecuter {
	private static Logger logger = Logger.getLogger(ScheduledJobExecuter.class);

    public static final String WORKFLOW_NAME = "activiti$activitiAdhoc";
    
    // private static final String WORKFLOW_DEF_NAME = "activiti$activitiReview";
    private final static String DESCRIPTION = "DESCRIPTION";
    
    static final QName PROP_WORKFLOW_TO = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "to");
    static final QName PROP_WORKFLOW_SUBJECT = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "subject");
    static final QName PROP_WORKFLOW_BODY = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "body");
    static final QName PROP_WORKFLOW_FROM = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "from");
    
    private NamespaceService namespaceService;
	private CalendarService calendarService;
	private WorkflowService workflowService;
    private NodeService nodeService;
    private PersonService personService;
    
    /**
     * Public API access
     */
    private ServiceRegistry serviceRegistry;

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * Executer implementation
     */
    public void execute() {
    	
        logger.info("Running the scheduled job");

		String[] sites = {"aecid-mercosur", "aecid-brasil", "aecid-argentina", "aecid-uruguay", "aecid-chile"};
		
		DateTime toDate = new DateTime();
		DateTime fromDate = toDate.plusDays(-7);
		
		SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy");

		String toDateStr = outputFormat.format(toDate.toDate());
		String fromDateStr = outputFormat.format(fromDate.toDate());
		
		logger.info("Fechas formateadas: " + fromDateStr + " - " + toDateStr);
		
		PagingRequest paging = new PagingRequest(20);
		
		PagingResults<CalendarEntry> result = calendarService.listCalendarEntries(sites, fromDate.toDate(), toDate.toDate(), paging);

		if (result == null || result.getPage() == null) {
			logger.info("Calendar entry not founded");
			return;
		}
			
		List<CalendarEntry> page = result.getPage();
		logger.info("Calendar entry founded: " + result.getPage().size());

		for (CalendarEntry entry : page) {
			
			// Create Task workflow instance
	        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
	        WorkflowInstance workflowInstance = createWorkflow(entry.getNodeRef(), entry.getTitle(), entry.getDescription(), fromDate.toDate(), toDate.toDate());
	        logger.info("Workflow creado: " + workflowInstance.getId() + " -- description: " + workflowInstance.getDescription());

		}
    }

    /**
     * @param workflowDefName
     * @return
     */
    private String getWorkflowDefIdByName(String workflowDefName) {
       // return workflowService.getAllDefinitions().stream()
       //                 .filter(workflowDefinition -> workflowDefinition.getName().equals(workflowDefName))
       //                 .findFirst().get().getId();
        
        WorkflowDefinition wfDefinition = workflowService.getDefinitionByName(WORKFLOW_NAME);
        
        if (wfDefinition == null) {
        	logger.error("Definicion Workflow no encontrada con el nombre: " + WORKFLOW_NAME);
        	return null;
        }
        
        logger.info("Workflow a utilizar: " + wfDefinition.getName());
        
        return wfDefinition.getId();
    }
    
    
    private WorkflowInstance createWorkflow(NodeRef noderef, String subject, String body, Date startDate, Date dueDate) {
        String workflowDefinitionId = getWorkflowDefIdByName(WORKFLOW_NAME);
        NodeRef assignee = personService.getPerson(AuthenticationUtil.getAdminUserName());
        return createWorkflow(workflowDefinitionId, assignee, subject, body, startDate, dueDate, noderef);
    }
    
    /**
     * Creates a workflow and attaches it to all of the node refs
     *
     * @param workflowParams - any extra parameters in a map
     * @param nodeRefs The node refs to attach the workflow to
     * @return the ID of the workflow that was created
     */
    private WorkflowInstance createWorkflow(String workflowDefId, final NodeRef assignee, String subject, String body, Date startDate, Date dueDate, final NodeRef... nodeRefs) {
       
    	final NodeRef wfPackage = createWorkflowPackage(Arrays.asList(nodeRefs));

        final Map<QName, Serializable> parameters = new HashMap<>();
        
        parameters.put(WorkflowModel.ASSOC_ASSIGNEE, assignee);
        parameters.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);
        parameters.put(WorkflowModel.PROP_CONTEXT, wfPackage);
        parameters.put(WorkflowModel.PROP_START_DATE, startDate);
        parameters.put(WorkflowModel.PROP_DUE_DATE, dueDate);
        parameters.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, subject);
        parameters.put(WorkflowModel.PROP_SEND_EMAIL_NOTIFICATIONS, false);
        
        parameters.put(PROP_WORKFLOW_BODY, body);
        parameters.put(PROP_WORKFLOW_SUBJECT, subject);
        parameters.put(PROP_WORKFLOW_FROM, "alfresco@aecid.uy");
        
        final WorkflowPath wfPath = workflowService.startWorkflow(workflowDefId, parameters);
        final String workflowId = wfPath.getInstance().getId();
        final WorkflowTask startTask = workflowService.getStartTask(workflowId);

        logger.info("Task creada: " + startTask.getId());
        
        workflowService.endTask(startTask.getId(), null);
        
/*        final Map<QName, Serializable> taskParameters = new HashMap<>();
        
        taskParameters.put(PROP_WORKFLOW_TO, to);
        taskParameters.put(PROP_WORKFLOW_BODY, body);
        taskParameters.put(PROP_WORKFLOW_SUBJECT, body);
        taskParameters.put(PROP_WORKFLOW_FROM, "alfresco@aecid.uy");
        
        workflowService.updateTask(startTask.getId(), taskParameters, null, null);
  */             
        return wfPath.getInstance();
    }
    
    private NodeRef createWorkflowPackage(final List<NodeRef> items) {
      //  ParameterCheck.mandatoryCollection("items", items);
      
    	final NodeRef wfPackage = workflowService.createPackage(null);

        for (final NodeRef item : items) {
            if ((item != null) && nodeService.exists(item)) {
                final String itemName = (String) nodeService.getProperty(item, ContentModel.PROP_NAME);
                nodeService.addChild(wfPackage, item, WorkflowModel.ASSOC_PACKAGE_CONTAINS,
                                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(itemName)));
            }
        }

        return wfPackage;
    }
    

	public CalendarService getCalendarService() {
		return calendarService;
	}

	public void setCalendarService(CalendarService calendarService) {
		this.calendarService = calendarService;
	}
	
	public WorkflowService getWorkflowService() {
		return workflowService;
	}

	public void setWorkflowService(WorkflowService workflowService) {
		this.workflowService = workflowService;
	}

	public NodeService getNodeService() {
		return nodeService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public PersonService getPersonService() {
		return personService;
	}

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	public NamespaceService getNamespaceService() {
		return namespaceService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}
}