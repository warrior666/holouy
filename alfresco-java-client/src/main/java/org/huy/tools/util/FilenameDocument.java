package org.huy.tools.util;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FilenameDocument {
	
	public static final String AUTHOR_DELIMITER = "&";
	public static final char AUTHORS_LIST_DELIMITER = '-';
	
	private static final char DATE_START_DELIMITER = '[';
	private static final char DATE_END_DELIMITER = ']';
 
	protected List<String> authors;
	
	protected String title;
	protected String date;

	
	public FilenameDocument(String fn) {
		fromFilename(fn);
	}

	public List<String> getAuthors() {
		return authors;
	}

	public void setAuthors(String[] authors) {
		setAuthors(Arrays.asList(authors));
	}
	
	public void setAuthors(List<String> aList) {
		if (aList != null) {
			authors = new ArrayList<String>();
			for (String a : aList) {
				//Se podria estandarizar los nombres antes de cargarlos
				authors.add(a);
			}
		}
	}

	public boolean hasAuthor() {
		if (authors == null || authors.size() == 0) {
			return false;
		}
		
		return true;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title.trim();
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date.trim();
	}
	
	public boolean hasDate() {
		if (date == null || date.equals("")) {
			return false;
		}
		return true;
	}

	
	public void fromFilename(String fn) {
		
		String aux = fn.replace('[', DATE_START_DELIMITER);
		aux = aux.replace(']', DATE_END_DELIMITER);
		
		String authors = "";
		String date = "";
		
		if (fn.indexOf(AUTHORS_LIST_DELIMITER) >= 0) {
			authors = fn.substring(0, fn.lastIndexOf(AUTHORS_LIST_DELIMITER));
			aux = fn.substring(fn.lastIndexOf(AUTHORS_LIST_DELIMITER));
		}
		
		if (aux.indexOf(DATE_START_DELIMITER) >= 0) {
			date = aux.substring(aux.indexOf(DATE_START_DELIMITER), aux.indexOf(DATE_END_DELIMITER));
			aux = aux.substring(0, aux.indexOf(DATE_START_DELIMITER) -1);	
		} 
		
		setTitle(aux);
		setDate(date);
		setAuthors(authors.split(AUTHOR_DELIMITER));

	}
	
	public String toFilename() {
		StringWriter sw = new StringWriter();
		boolean firstAuthor = true;
		
		if (hasAuthor()) {
		for (String a : getAuthors()) {
			if (!firstAuthor) {
				sw.append(" " + AUTHOR_DELIMITER + " ");
			}
			
			sw.append(a);
			firstAuthor = false;
		}
		sw.append(" " + AUTHORS_LIST_DELIMITER + " ");
		
		}
		sw.append(getTitle());
		
		if (hasDate()) {
			sw.append(" " + DATE_START_DELIMITER + getDate() + DATE_END_DELIMITER + " ");
		}
		
		return sw.toString();
	}
}
