package org.holouy.skp.onto.types;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

public class HasNarrowerRelationshipType extends ConceptToConceptRelationshipType {

	public HasNarrowerRelationshipType(Model model, Resource resource, Resource inverseResource) {
		super(model, resource, inverseResource);
	}

}
