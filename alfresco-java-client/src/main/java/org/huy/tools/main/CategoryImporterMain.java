package org.huy.tools.main;

import org.huy.tools.category.CategoryImportTool;
import org.huy.tools.controller.CategoryController;

public class CategoryImporterMain {

	
	public static void main(String[] args) throws Exception {

		CategoryController.initInstance();
		
		if (args.length < 1) {
			printUsage();
			throw new Exception("Argumentos insuficientes");
		}
		String categoryFile = args[0];

		CategoryController.process(categoryFile);
	}
	

	   protected static void printUsage()
	   {
	      System.out.println("java "+CategoryImportTool.class.getName()+" <filename>");
	   }
}
