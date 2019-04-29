package org.holouy.skp.onto.types;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

public class HasBroaderRelationshipType extends ConceptToConceptRelationshipType {

	public HasBroaderRelationshipType(Model model, Resource resource, Resource inverseResource) {
		super(model, resource, inverseResource);
	}

}
