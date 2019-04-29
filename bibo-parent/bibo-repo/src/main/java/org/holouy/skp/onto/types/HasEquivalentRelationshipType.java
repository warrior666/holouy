package org.holouy.skp.onto.types;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

public class HasEquivalentRelationshipType extends RelationshipType {

	public HasEquivalentRelationshipType(Model model, Resource resource) {
		super(model, resource, null);
	}

}
