package org.aecid.alfresco;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.calendar.CalendarEntry;
import org.alfresco.service.cmr.calendar.CalendarEntryDTO;
import org.alfresco.service.cmr.calendar.CalendarService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;


public class NotificationActionExecuter extends ActionExecuterAbstractBase {

	private static Logger logger = Logger.getLogger(NotificationActionExecuter.class);

    public static final String PARAM_END_START_TASK = "endStartTask";
    public static final String PARAM_START_TASK_TRANSITION = "startTaskTransition";
    
	public static final String PARAM_EMAIL_TO_NAME = "to";
	public static final String PARAM_EMAIL_SUBJECT_NAME = "subject";
	public static final String PARAM_EMAIL_BODY_NAME = "body_text";
	public static final String PARAM_NOTIFICATION_TYPE_NAME = "notification_type";
	public static final String PARAM_DATE_NAME = "date";
	public static final String PARAM_SITE_NAME = "site";
	
	private CalendarService calendarService;
    
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		for (String s : new String[] { PARAM_EMAIL_TO_NAME, PARAM_EMAIL_SUBJECT_NAME, PARAM_EMAIL_BODY_NAME,
				PARAM_NOTIFICATION_TYPE_NAME, PARAM_DATE_NAME, PARAM_SITE_NAME }) {
			paramList.add(new ParameterDefinitionImpl(s, DataTypeDefinition.TEXT, true, getParamDisplayLabel(s)));
		}
	}

	@Override
	protected void executeImpl(Action action, NodeRef nodeRef) {
		
		String to = (String) action.getParameterValue(PARAM_EMAIL_TO_NAME);
		String subject = (String) action.getParameterValue(PARAM_EMAIL_SUBJECT_NAME);
		String type = (String) action.getParameterValue(PARAM_NOTIFICATION_TYPE_NAME);
		String body = (String) action.getParameterValue(PARAM_EMAIL_BODY_NAME);
		String dateStr = (String) action.getParameterValue(PARAM_DATE_NAME);
		String site = (String) action.getParameterValue(PARAM_SITE_NAME);
		
		if (site == null || site.isEmpty()) {
			site = "mercosur";
		}
		
		logger.info("Email: " + to);
		logger.info("Subject: " + subject);
		logger.info("body: " + body);
		logger.info("Type: " + type);
		logger.info("Date: " + dateStr);
		logger.info("Site: " + site);
		
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
		logger.info("Fecha formateada: " + formattedDate);

		switch (type) {
		case "Fichas de preidentificacion":
			body = "Las Fichas de pre identificacion deben presentarse antes del: " + formattedDate + "\n" + body;
		break;
		
		case "Seguimiento de ejecucion":
			body = "Las Fichas de seguimiento deben presentarse antes del: " + formattedDate + "\n" + body;
			break;
		
		case "Nota Pais":
			body = "Las Nota Pais deben presentarse antes del: " + formattedDate + "\n" + body;
			break;
		
		case "Otras Notificaciones":
		default:
			body = "Notificacion general: \n" + body; 
		}

		// Create a calendar entry
		CalendarEntry entry = new CalendarEntryDTO(subject, body, "Location", date, date);
		calendarService.createCalendarEntry("aecid-" + site.toLowerCase(), entry);
		
		if (entry != null) {
			if (entry.getNodeRef() == null) {
				logger.info("Calendar entry created title: " + entry.getTitle());	
			} else {
				logger.info("Calendar entry created: " + entry.getNodeRef().getId() + " -- title: " + entry.getTitle());
			}
		} else {
			logger.info("Error creando la entrada en el calendario");
		}
	}
        
	public CalendarService getCalendarService() {
		return calendarService;
	}

	public void setCalendarService(CalendarService calendarService) {
		this.calendarService = calendarService;
	}


	/*
	public TaskService getTaskService() {
		return taskService;
	}

	public void setTaskService(TaskService taskService) {
		this.taskService = taskService;
	}
*/
}