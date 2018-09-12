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
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.invitation.WorkflowModelModeratedInvitation;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.calendar.CalendarEntry;
import org.alfresco.service.cmr.calendar.CalendarEntryDTO;
import org.alfresco.service.cmr.calendar.CalendarService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
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
import org.alfresco.util.ParameterCheck;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;


public class NotificationActionExecuter extends ActionExecuterAbstractBase {

	private static Logger logger = Logger.getLogger(NotificationActionExecuter.class);

    public static final String WORKFLOW_NAME = "activiti$activitiAdhoc";
    
    // private static final String WORKFLOW_DEF_NAME = "activiti$activitiReview";
    private final static String DESCRIPTION = "DESCRIPTION";
    
    
    public static final String PARAM_END_START_TASK = "endStartTask";
    public static final String PARAM_START_TASK_TRANSITION = "startTaskTransition";
    
	public static final String PARAM_EMAIL_TO_NAME = "to";
	public static final String PARAM_EMAIL_SUBJECT_NAME = "subject";
	public static final String PARAM_EMAIL_BODY_NAME = "body_text";
	public static final String PARAM_NOTIFICATION_TYPE_NAME = "notification_type";
	public static final String PARAM_DATE_NAME = "date";

	private CalendarService calendarService;
	private WorkflowService workflowService;
    private NamespaceService namespaceService;
    private NodeService nodeService;
    private PersonService personService;
    
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		for (String s : new String[] { PARAM_EMAIL_TO_NAME, PARAM_EMAIL_SUBJECT_NAME, PARAM_EMAIL_BODY_NAME,
				PARAM_NOTIFICATION_TYPE_NAME, PARAM_DATE_NAME }) {
			paramList.add(new ParameterDefinitionImpl(s, DataTypeDefinition.TEXT, true, getParamDisplayLabel(s)));
		}
	}

	@Override
	protected void executeImpl(Action action, NodeRef nodeRef) {
		
		String subject = (String) action.getParameterValue(PARAM_EMAIL_SUBJECT_NAME);
		String body = (String) action.getParameterValue(PARAM_EMAIL_BODY_NAME);
		String dateStr = (String) action.getParameterValue(PARAM_DATE_NAME);

		logger.info("Email: " + action.getParameterValue(PARAM_EMAIL_TO_NAME));
		logger.info("Subject: " + subject);
		logger.info("body: " + body);
		logger.info("Type: " + action.getParameterValue(PARAM_NOTIFICATION_TYPE_NAME));
		logger.info("Date: " + dateStr);

		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy");
		Date date;

		try {
			date = inputFormat.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
			logger.error("Error formato de fecha", e);
			date = new Date();
		}
		DateTime dueDate = new DateTime(date);
		DateTime startDate = dueDate.plusDays(-7);
		
		String formattedDate = outputFormat.format(date);
		logger.info("Fecha formateada" + formattedDate);

		// Create a calendar entry
		CalendarEntry entry = new CalendarEntryDTO(subject, body, "Location", date, date);

		calendarService.createCalendarEntry("aecid-mercosur", entry);

        
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

        WorkflowInstance workflowInstance = createWorkflow(nodeRef, subject, startDate.toDate(), dueDate.toDate());
        
        logger.info("Workflow creado: " + workflowInstance.getId() + " -- description: " + workflowInstance.getDescription());
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
    
    
    private WorkflowInstance createWorkflow(NodeRef noderef, String description, Date startDate, Date dueDate) {
        String workflowDefinitionId = getWorkflowDefIdByName(WORKFLOW_NAME);
        NodeRef assignee = personService.getPerson(AuthenticationUtil.getAdminUserName());
        return createWorkflow(workflowDefinitionId, assignee, description, startDate, dueDate, noderef);
    }
    
    /**
     * Creates a workflow and attaches it to all of the node refs
     *
     * @param workflowParams - any extra parameters in a map
     * @param nodeRefs The node refs to attach the workflow to
     * @return the ID of the workflow that was created
     */
    private WorkflowInstance createWorkflow(String workflowDefId, final NodeRef assignee, String description, Date startDate, Date dueDate, final NodeRef... nodeRefs) {
       
    	final NodeRef wfPackage = createWorkflowPackage(Arrays.asList(nodeRefs));

        final Map<QName, Serializable> parameters = new HashMap<>();
        
        parameters.put(WorkflowModel.ASSOC_ASSIGNEE, assignee);
        parameters.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);
        parameters.put(WorkflowModel.PROP_CONTEXT, wfPackage);
        parameters.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, description);
        parameters.put(WorkflowModel.PROP_START_DATE, startDate);
        parameters.put(WorkflowModel.PROP_DUE_DATE, dueDate);
        parameters.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, description);
        parameters.put(WorkflowModel.PROP_SEND_EMAIL_NOTIFICATIONS, false);

        final WorkflowPath wfPath = workflowService.startWorkflow(workflowDefId, parameters);
        final String workflowId = wfPath.getInstance().getId();
        final WorkflowTask startTask = workflowService.getStartTask(workflowId);
        workflowService.endTask(startTask.getId(), null);
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
    
//	public void start2wf() {
//		// create workflow package out of package items
//		NodeRef packageNodeRef = WorkflowHelper.createWorkflowPackage(packageItemList);
//
//		Map<QName, Serializable> parameters = new HashMap<QName, Serializable>();
//
//		parameters.put(WorkflowModel.ASSOC_PACKAGE, packageNodeRef);
//		parameters.put(WorkflowModel.PROP_DESCRIPTION, taskComment);
//		parameters.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, taskComment);
//
//		parameters.put(ContentModel.PROP_OWNER, initiatorUsername);
//
//		// if it is workflow with one assignee
//		if(workflow41AssigneeFlag) {
//		   logger.debug("Workflow with one assignee.");
//		   NodeRef nrAssignee = AlfrescoServices.getPersonService().getPerson(assigneeUsernameList.get(0));
//		   parameters.put(WorkflowModel.ASSOC_ASSIGNEE, nrAssignee);
//		} else {
//		   logger.debug("Workflow with multiple assignees.");
//		   // if it is workflow with multiple assignees
//		   ArrayList<NodeRef> assignees = new ArrayList<NodeRef>();
//		               
//		   for(String assigneeUsername : assigneeUsernameList) {
//		      NodeRef nrAssignee = AlfrescoServices.getPersonService().getPerson(assigneeUsername);
//		            
//		      if(nrAssignee != null) {
//		         assignees.add(nrAssignee);
//		      }
//		   }
//		               
//		   parameters.put(QName.createQName(BPM_ASSIGNEES_QNAME), assignees);
//		}
//
//		// task start date
//		parameters.put(WorkflowModel.PROP_START_DATE, new Date());
//		                              
//		// task and workflow due date
//		if(dueDate != null) {
//		   logger.debug("Due date: " + dueDate);
//		   parameters.put(WorkflowModel.PROP_DUE_DATE, dueDate);
//		   parameters.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, dueDate);
//		} else {
//		   logger.warn("Due date not set.");
//		}
//		                              
//		// set permission on Company Home for initiator
//		logger.debug("Setting permissions…");
//		NodeRef nrCompanyHome = WorkflowHelper.setPermissionForUserOnCompanyHomeNode(initiatorUsername, COMPANY_HOME_SET_PERMISSION);
//		logger.debug("Setting permissions…done.");
//
//		// start workflow
//		logger.debug("Workflow definition Id: " + workflowDefinition.id);
//
//		WorkflowPath path = null;
//
//		path = AlfrescoServices.getWorkflowService().startWorkflow(workflowDefinition.id, parameters);
//
//		// end start task
//		List<WorkflowTask> tasks = AlfrescoServices.getWorkflowService().getTasksForWorkflowPath(path.id);
//		WorkflowTask startTask = tasks.get(0);
//		                        
//		AlfrescoServices.getWorkflowService().endTask(startTask.id, null);
//		            
//		// delete permission on Company Home for initiator
//		logger.debug("Deleting permissions…");
//		WorkflowHelper.deletePermissionForUserOnCompanyHomeNode(nrCompanyHome, initiatorUsername, initiatorUsername, COMPANY_HOME_SET_PERMISSION);
//		logger.debug("Deleting permissions…done.");
//		            
//		logger.info(String.format("Workflow %s started by user: %s", workflowName, initiatorUsername));	
//	}
//	
//    public startwf() {
//        NodeRef wfPackage = workflowService.createPackage(null);
//
//        Map<QName, Serializable> workflowProps = new HashMap<QName, Serializable>(16);
//        
//        workflowProps.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);
//        workflowProps.put(WorkflowModel.ASSOC_ASSIGNEE, inviteeNodeRef);
//        workflowProps.put(
//              WorkflowModelModeratedInvitation.ASSOC_GROUP_ASSIGNEE,
//              roleGroup);
//        workflowProps.put(
//              WorkflowModelModeratedInvitation.WF_PROP_INVITEE_COMMENTS,
//              inviteeComments);
//        workflowProps.put(
//              WorkflowModelModeratedInvitation.WF_PROP_INVITEE_ROLE,
//              inviteeRole);
//        workflowProps.put(
//              WorkflowModelModeratedInvitation.WF_PROP_INVITEE_USER_NAME,
//              inviteeUserName);
//        workflowProps.put(
//              WorkflowModelNominatedInvitation.WF_PROP_RESOURCE_NAME,
//              resourceName);
//        workflowProps.put(
//              WorkflowModelNominatedInvitation.WF_PROP_RESOURCE_TYPE,
//              resourceType.toString());
//
//        // get the moderated workflow
//
//        WorkflowDefinition wfDefinition = this.workflowService
//              .getDefinitionByName(WorkflowModelModeratedInvitation.WORKFLOW_DEFINITION_NAME);
//        if (wfDefinition == null) {
//           // handle workflow definition does not exist
//           Object objs[] = { WorkflowModelModeratedInvitation.WORKFLOW_DEFINITION_NAME };
//           throw new InvitationException("invitation.error.noworkflow", objs);
//        }
//
//        // start the workflow
//        WorkflowPath wfPath = this.workflowService.startWorkflow(wfDefinition
//              .getId(), workflowProps);
//
//        String workflowId = wfPath.instance.id;
//        String wfPathId = wfPath.id;
//        List<WorkflowTask> wfTasks = this.workflowService
//              .getTasksForWorkflowPath(wfPathId);
//
//        // throw an exception if no tasks where found on the workflow path
//        if (wfTasks.size() == 0) 
//        {
//           Object objs[] = { WorkflowModelModeratedInvitation.WORKFLOW_DEFINITION_NAME };
//           throw new InvitationException("invitation.error.notasks", objs);
//        }
//
//        try {
//           WorkflowTask wfStartTask = wfTasks.get(0);
//           this.workflowService.endTask(wfStartTask.id, null);
//        } catch (RuntimeException err) {
//           if (logger.isDebugEnabled())
//              logger.debug("Failed - caught error during Invite workflow transition: "
//                 + err.getMessage());
//           throw err;
//        }
//        
//        
//        // determine whether to auto-end the start task
//      //  Boolean endStartTask = (Boolean)action.getParameterValue(PARAM_END_START_TASK);
//      //  String startTaskTransition = (String)action.getParameterValue(PARAM_START_TASK_TRANSITION);
//      //  endStartTask = (endStartTask == null) ? true : false;
//        
//        // auto-end the start task with the provided transition (if one)
//       // if (endStartTask)
//       // {
//       //     List<WorkflowTask> tasks = workflowService.getTasksForWorkflowPath(path.getId());
//       //     for (WorkflowTask task : tasks)
//       //     {
//       //         workflowService.endTask(task.getId(), startTaskTransition);
//       //     }
//       // }
//	}


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

	public NamespaceService getNamespaceService() {
		return namespaceService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
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
}