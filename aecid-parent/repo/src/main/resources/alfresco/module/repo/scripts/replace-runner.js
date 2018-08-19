logger.log("**** LOGGER ALFRESCO Replace runner *****");
logger.log("DOCUMENT NAME: " + document.name);
logger.log("DOCUMENT NODEREF: " + document.nodeRef);

var foundNode = search.findNode(document.nodeRef);
var moveAction = actions.create("move-replaced");

moveAction.execute(foundNode);