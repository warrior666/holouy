package org.huy.tools.main;

import java.io.File;

import org.huy.tools.controller.LoaderController;

public class FileLoaderMain {

	
	public static void main(String[] args) throws Exception {

		LoaderController.initInstance();
		
		if (args.length < 2)
			throw new Exception("Argumentos insuficientes");

		String folderPath = args[0];

		File folder = new File(folderPath);
		
		LoaderController.process(folder);
	}

}
