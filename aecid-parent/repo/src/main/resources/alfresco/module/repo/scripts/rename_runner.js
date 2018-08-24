logger.log("LOGGER ALFRESCO RENAME *****");
logger.log("DOCUMENT NAME: " + document.name);
logger.log("DOCUMENT NODEREF: " + document.nodeRef);

var foundNode = search.findNode(document.nodeRef);
var moveAction = actions.create("renamer");
moveAction.execute(foundNode);