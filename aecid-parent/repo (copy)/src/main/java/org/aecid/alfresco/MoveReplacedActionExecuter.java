package org.aecid.alfresco;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;


public class MoveReplacedActionExecuter extends ActionExecuterAbstractBase {

	private final static Logger log = Logger.getLogger(MoveReplacedActionExecuter.class);
	
	
	public static final String NAME = "move-replaced";

	private static final String AECID_MODEL_1_0_URI = "http://aecid.org/model/aecid/1.0";
    
	static final QName TYPE_CONTENT = QName.createQName(AECID_MODEL_1_0_URI, "content");
    

	private FileFolderService fileFolderService;
	private NodeService nodeService;
	private ContentService contentService;

	/**
     * @see org.alfresco.repo.action.executer.ActionExecuter#execute(org.alfresco.repo.ref.NodeRef, org.alfresco.repo.ref.NodeRef)
     */
    public void executeImpl(Action ruleAction, NodeRef nodeRef) {

        log.info("Nodo a copiar id: " + nodeRef.getId());
        
        ChildAssociationRef childAssociationRef = nodeService.getPrimaryParent(nodeRef);
        NodeRef parent = childAssociationRef.getParentRef();
        
		// Fetch all the properties
		Map<QName, Serializable> props = nodeService.getProperties(nodeRef);

		// Fetch a few common properties
		String name = (String) props.get(ContentModel.PROP_NAME);
		
        NodeRef newNode = copyContentNode(parent, name, nodeRef);
        
        log.info("Nodo copiado storeRef: " + newNode.getStoreRef());
	}

	private NodeRef copyContentNode(NodeRef parent, String name, NodeRef origin) {

		// Create a map to contain the values of the properties of the node
		Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
		props.put(ContentModel.PROP_NAME, name + "_bk");

		// use the node service to create a new node
		NodeRef node = nodeService.createNode(parent, ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), TYPE_CONTENT, props)
				.getChildRef();

		ContentReader reader = contentService.getReader(origin, ContentModel.PROP_CONTENT);

		// Use the content service to set the content onto the newly created node
		ContentWriter writer = contentService.getWriter(node, ContentModel.PROP_CONTENT, true);
		writer.setMimetype(reader.getMimetype());
		writer.setEncoding(reader.getEncoding());
		writer.putContent(reader.getContentInputStream());

		// Replace original node
		nodeService.deleteNode(origin);
		
		// Remove -bk from name
		nodeService.setProperty(node, ContentModel.PROP_NAME, name);
		
		// Return a node reference to the newly created node
		return node;
	}

	private NodeRef createContentNode(NodeRef parent, String name, String text) {

		// Create a map to contain the values of the properties of the node

		Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
		props.put(ContentModel.PROP_NAME, name + "bk");

		// use the node service to create a new node
		NodeRef node = nodeService.createNode(parent, ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), ContentModel.TYPE_CONTENT, props)
				.getChildRef();

		// Use the content service to set the content onto the newly created node
		ContentWriter writer = contentService.getWriter(node, ContentModel.PROP_CONTENT, true);
		writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
		writer.setEncoding("UTF-8");
		writer.putContent(text);

		// Return a node reference to the newly created node
		return node;
	}

	public void setFileFolderService(FileFolderService service) {
		fileFolderService = service;
	}

	public void setNodeService(NodeService service) {
		nodeService = service;
	}

	public void setContentService(ContentService service) {
		contentService = service;
	}

	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		// TODO Auto-generated method stub

	}

}
