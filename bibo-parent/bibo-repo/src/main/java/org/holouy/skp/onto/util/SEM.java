package org.holouy.skp.onto.util;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public class SEM  {

	public static Property guid = ResourceFactory.createProperty("http://www.smartlogic.com/2014/08/semaphore-core#guid");

	public static Property defaultProperty = ResourceFactory.createProperty("http://www.smartlogic.com/2014/08/semaphore-core#DefaultProperty");
	
	public static Property alwaysVisibleProperty = ResourceFactory.createProperty("http://www.smartlogic.com/2014/08/semaphore-core#AlwaysVisibleProperty");
}
