package org.holouy.skp.onto.concept;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.holouy.skp.onto.types.RelationshipType;

public abstract class ConceptToConceptRelationshipType extends RelationshipType {

	public ConceptToConceptRelationshipType(Model model, Resource resource, Resource inverseResource) {
		super(model, resource, inverseResource);
	}

}
