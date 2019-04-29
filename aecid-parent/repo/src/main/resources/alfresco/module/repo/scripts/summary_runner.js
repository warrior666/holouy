logger.log("LOGGER ALFRESCO SUMMARY *****");
logger.log("DOCUMENT NAME: " + document.name);
logger.log("DOCUMENT NODEREF: " + document.nodeRef);

var foundNode = search.findNode(document.nodeRef);
var moveAction = actions.create("summary");
moveAction.execute(foundNode);