package org.holouy.alfresco.loader;

import java.util.Map;

public class CreateContent {

	/**
	 * Creates a new content node setting the content provided.
	 *
	 * @param  parent   the parent node reference
	 * @param  name     the name of the newly created content object
	 * @param  text     the content text to be set on the newly created node
	 * @return NodeRef  node reference to the newly created content node
	 */
	 
	private NodeRef createContentNode(NodeRef parent, String name, String text)
	{

	    // Create a map to contain the values of the properties of the node
	        
	    Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
	    props.put(ContentModel.PROP_NAME, name);  

	    // use the node service to create a new node
	    NodeRef node = this.nodeService.createNode(
	                        parent, 
	                        ContentModel.ASSOC_CONTAINS, 
	                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
	                        ContentModel.TYPE_CONTENT, 
	                        props).getChildRef();
	                        
	    // Use the content service to set the content onto the newly created node
	    ContentWriter writer = this.contentService.getWriter(node, ContentModel.PROP_CONTENT, true);
	    writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
	    writer.setEncoding("UTF-8");
	    writer.putContent(text);
	    
	    // Return a node reference to the newly created node
	    return node;
	}
}