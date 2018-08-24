package org.aecid.alfresco;
import java.util.List;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.log4j.Logger;


public class SendAsEmailActionExecuter extends ActionExecuterAbstractBase {
    
	private static Logger logger = Logger.getLogger(SendAsEmailActionExecuter.class);

    public static final String PARAM_EMAIL_TO_NAME = "to";
    public static final String PARAM_EMAIL_SUBJECT_NAME = "subject";
    public static final String PARAM_EMAIL_BODY_NAME = "body_text";

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        for (String s : new String[]{PARAM_EMAIL_TO_NAME, PARAM_EMAIL_SUBJECT_NAME, PARAM_EMAIL_BODY_NAME}) {
            paramList.add(new ParameterDefinitionImpl(s, DataTypeDefinition.TEXT, true, getParamDisplayLabel(s)));
        }
    }

	@Override
	protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
		// TODO Auto-generated method stub
		
	}

}