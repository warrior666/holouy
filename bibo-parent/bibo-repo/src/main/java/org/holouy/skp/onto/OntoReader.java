package org.holouy.skp.onto;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.ModelCom;
import org.apache.jena.rdf.model.impl.StatementBase;
import org.apache.jena.rdf.model.impl.StatementImpl;

public class OntoReader {

	public Concept createConcept(URI uri, Label prefLabel, URI[] classURIs, UUID uuid) throws ModelException {
		Resource conceptURIResource = resourceFromURI(model, uri);

		if (resourceInUse(conceptURIResource)) throw new ModelException("Attempting to create concept with URI - '%s'. This URI is already in use.", uri.toString());
		
		if ((classURIs == null) || (classURIs.length == 0)) {
			conceptURIResource.addProperty(RDF.type, SKOS.Concept);
		} else {
			for (URI classURI: classURIs) {
				conceptURIResource.addProperty(RDF.type, resourceFromURI(model, classURI));
			}
		}
		
		conceptURIResource.addLiteral(SEM.guid, (uuid == null ? Utils.generateGuid(uri.toString()) : uuid.toString()));
			
		URI labelURI = getLabelURI(model, conceptURIResource, SKOS.prefLabel, prefLabel);
		Resource labelURIResource = resourceFromURI(model, labelURI);
		model.add(conceptURIResource, SKOSXL.prefLabel, labelURIResource);
		model.add(labelURIResource, SKOSXL.literalForm, getAsLiteral(model, prefLabel));
		model.add(labelURIResource, RDF.type, SKOSXL.Label);
		return new Concept(model, conceptURIResource);
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Model model = ModelFactory.createDefaultModel() ;
		model.read("data.ttl") ;
		
		ModelCom mc = new ModelCom();
		Resource r = new ResourceImpl();
		Statement s = new StatementImpl();
		model.add(s);

		
	
	}

}
