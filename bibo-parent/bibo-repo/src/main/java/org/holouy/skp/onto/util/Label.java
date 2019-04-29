package org.holouy.skp.onto.util;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.holouy.skp.onto.exception.ModelException;

public class Label {

	public Label(String value, Language language) {
		this.resource = null;
		this.uri = null;
		this.value = value;
		this.language = language;
	}
	
	public Label(Resource resource, String value, Language language) throws ModelException {
		try {
			this.resource = resource;
			this.uri = new URI(resource.getURI());
			this.value = value;
			this.language = language;
		} catch (URISyntaxException e) {
			throw new ModelException("Unable to generate URI from resource: '%s'", resource.toString());
		}
	}
	
	public Label(Literal literal) {
		this.resource = null;
		this.uri = null;
		this.value = literal.getString();
		this.language = Language.getLanguage(literal.getLanguage());
	}

	private final Resource resource;
	private final URI uri;
	private final String value;
	private final Language language;

	public Resource getResource() {
		return resource;
	}
	public String getValue() {
		return value;
	}
	public Language getLanguage() {
		return language;
	}

	public URI getURI() {
		return uri;
	}

	@Override
	public String toString() {
		return String.format("\"%s\"%s", value, language.getCode());
	}

	public String getLanguageCode() {
		return language.getCode();
	}
}
