package org.huy.tools.main;

import org.huy.tools.controller.CategoryController;


public class CategoryImporterMain {

	public static void main(String[] args) throws Exception {

		if (args.length < 2) {
			printUsage();
			throw new Exception("Argumentos insuficientes");
		}

		String group = args[0];
		String categoryFile = args[1];
		
		if (args.length < 5) {
			CategoryController.initInstance();
			
		} else {
			String endpoint = args[2];
			String user = args[3];
			String password = args[4];

			CategoryController.initInstance(endpoint, user, password);
		}	
		
		CategoryController.process(group, categoryFile);
	}

	protected static void printUsage() {
		System.out.println(
				"Usage: java -cp target/alfresco-toolkit.jar org.huy.tools.main.CategoryImporterMain [group] [filename] [endpoint] [user] [password]");
	}
}
