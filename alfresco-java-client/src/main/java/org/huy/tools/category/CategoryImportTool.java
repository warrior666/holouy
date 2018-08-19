package org.huy.tools.category;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.huy.tools.controller.CategoryController;
import org.huy.tools.model.CategoryDTO;

import com.alfresco.client.api.common.representation.ResultPaging;
import com.alfresco.client.api.core.model.representation.TagRepresentation;
import com.alfresco.client.api.search.body.QueryBody;
import com.alfresco.client.api.search.body.RequestQuery;
import com.alfresco.client.api.search.body.RequestQuery.LanguageEnum;
import com.alfresco.client.api.search.model.ResultNodeRepresentation;
import com.alfresco.client.api.search.model.ResultSetRepresentation;

import retrofit2.Call;
import retrofit2.Response;

public class CategoryImportTool {

	private static final String NEW_LINE = "\n";

	private final static Logger log = Logger.getLogger(CategoryImportTool.class);

	private CategoryController controller;

	public CategoryImportTool(CategoryController client) {
		controller = client;
	}

	public void readTags() {
		Call<ResultPaging<TagRepresentation>> tags = controller.getTagsAPI().listTagsCall();

		System.out.println(tags.toString());
		try {
			Response<ResultPaging<TagRepresentation>> response = tags.execute();

			for (TagRepresentation rNode : response.body().getList()) {
				System.out.println("Tags: " + rNode.getTag());

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public List<CategoryDTO> readCategoriesFromFile(String fileName) throws IOException {
		
		BufferedReader in = new BufferedReader(new FileReader(fileName));
		List<CategoryDTO> categories = new ArrayList<CategoryDTO>();
		
		String str = null;
		
		while ((str = in.readLine()) != null) {
			if (str.trim().length() == 0)
				continue; // skip blank lines
			if (str.trim().startsWith("#"))
				continue; // skip comments starting with the # character

			categories.add(new CategoryDTO(str));
		}
		
		in.close();
		
		return categories;
	}


	public void writeCategoriesToFile(List<CategoryDTO> categories, String fileName) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
		
//		for (CategoryDTO cat : categories) {
//			out.write(cat.toString());
//		}
		out.write(categories.stream()
				  .map(CategoryDTO::toString)
				  .collect(Collectors.joining(NEW_LINE)));
		
		out.close();
	}
}