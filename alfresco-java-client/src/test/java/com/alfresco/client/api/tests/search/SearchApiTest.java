/*
 *   Copyright (C) 2005-2016 Alfresco Software Limited.
 *
 *   This file is part of Alfresco Java Client.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.alfresco.client.api.tests.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alfresco.client.api.AlfrescoAPITestCase;
import com.alfresco.client.api.search.body.QueryBody;
import com.alfresco.client.api.search.body.RequestFacetFields;
import com.alfresco.client.api.search.body.RequestFacetFieldsFacets;
import com.alfresco.client.api.search.body.RequestFacetQuery;
import com.alfresco.client.api.search.body.RequestPagination;
import com.alfresco.client.api.search.body.RequestQuery;
import com.alfresco.client.api.search.body.RequestSortDefinition;
import com.alfresco.client.api.search.model.ResultNodeRepresentation;
import com.alfresco.client.api.search.model.ResultSetRepresentation;

import retrofit2.Response;

public class SearchApiTest extends AlfrescoAPITestCase
{

    @BeforeClass
    public void prepare() throws Exception
    {
        client = prepareClient(TEST_ENDPOINT, TEST_USERNAME, TEST_PASSWORD);
    }

    @Test
    public void querySearchByTitleTest() throws IOException
    {
    	String title="Participation and Democratic";
    	String queryStr = "(name:\"*" + title + "*\" OR title:\"*" + title + "*\")";
    	
        RequestQuery query = new RequestQuery().query(queryStr);
        QueryBody body = new QueryBody().query(query);

        // Request
        Response<ResultSetRepresentation<ResultNodeRepresentation>> response = client.getSearchAPI().searchCall(body)
                .execute();
        Assert.assertNotNull(response);
        Assert.assertEquals(response.isSuccessful(), true);

        // Check Response
        ResultSetRepresentation<ResultNodeRepresentation> resultSet = response.body();
        Assert.assertNotNull("Response is empty", resultSet);
        Assert.assertNotNull("Response has no entries  ", resultSet.getList());
        Assert.assertNotNull("Response has no Pagination Info", resultSet.getPagination());
     //   Assert.assertNull(resultSet.getContext(), "Response has Context");

        // Check Pagination & Entries
        List<ResultNodeRepresentation> results = resultSet.getList();
        
        System.out.println("RESULTADO:");
        System.out.println("Name:" + results.get(0).getName());
        System.out.println("ID:" + results.get(0).getId());
    }
    
    @Test
    public void querySearchTest() throws IOException
    {
        RequestQuery query = new RequestQuery().query("foo").language(RequestQuery.LanguageEnum.AFTS);
        QueryBody body = new QueryBody().query(query);

        // Request
        Response<ResultSetRepresentation<ResultNodeRepresentation>> response = client.getSearchAPI().searchCall(body)
                .execute();
        Assert.assertNotNull(response);
        Assert.assertEquals(response.isSuccessful(), true);

        // Check Response
        ResultSetRepresentation<ResultNodeRepresentation> resultSet = response.body();
        Assert.assertNotNull("Response is empty", resultSet);
        Assert.assertNotNull("Response has no entries  ", resultSet.getList());
        Assert.assertNotNull("Response has no Pagination Info", resultSet.getPagination());
        Assert.assertNull("Response has Context", resultSet.getContext());

        // Check Pagination & Entries
        List<ResultNodeRepresentation> results = resultSet.getList();
    }

    @Test
    public void queryCMISSearchTest() throws IOException
    {
        RequestQuery query = new RequestQuery().query("select * from cmis:folder").language(RequestQuery.LanguageEnum.CMIS);
        QueryBody body = new QueryBody().query(query);

        // Request
        Response<ResultSetRepresentation<ResultNodeRepresentation>> response = client.getSearchAPI().searchCall(body)
                .execute();
        Assert.assertNotNull(response);
        Assert.assertEquals(response.isSuccessful(), true);

        // Check Response
        ResultSetRepresentation<ResultNodeRepresentation> resultSet = response.body();
        Assert.assertNotNull("Response is empty", resultSet);
        Assert.assertNotNull("Response has no entries  ", resultSet.getList());
        Assert.assertNotNull("Response has no Pagination Info", resultSet.getPagination());
        Assert.assertNull("Response has Context", resultSet.getContext());

        // Check Pagination & Entries
        List<ResultNodeRepresentation> results = resultSet.getList();
    }

    @Test
    public void queryContextSearchTest() throws IOException
    {
        // Prepare Query
        RequestQuery query = new RequestQuery().query("alfresco").language(RequestQuery.LanguageEnum.AFTS);

        // Prepare Facets
        List<RequestFacetQuery> facetsQuery = Arrays.asList(
                new RequestFacetQuery().query("content.size:[0 TO 10240]").label("xtra small"),
                new RequestFacetQuery().query("content.size:[10240  TO 102400]").label("small"),
                new RequestFacetQuery().query("content.size:[102400  TO 1048576]").label("medium"),
                new RequestFacetQuery().query("content.size:[1048576  TO 16777216]").label("large"),
                new RequestFacetQuery().query("content.size:[16777216   TO 134217728]").label("xtra large"),
                new RequestFacetQuery().query("content.size:[134217728   TO MAX]").label("XX large"));

        RequestFacetFields facetFields = new RequestFacetFields();
        facetFields.facets(Arrays.asList(new RequestFacetFieldsFacets().field("'content.size'")));

        // Filter Fields
        List<String> fields = Arrays.asList("id", "name", "search");

        // Pagination
        RequestPagination pagination = new RequestPagination().maxItems(5).skipCount(1);

        // Sort
        ArrayList<RequestSortDefinition> sorting = new ArrayList<RequestSortDefinition>(6)
        {
            {
                add(new RequestSortDefinition().type(RequestSortDefinition.TypeEnum.SCORE).field("cm:name")
                        .ascending(true));
            }
        };

        // Prepare Body
        QueryBody body = new QueryBody().query(query).facetQueries(facetsQuery).facetFields(facetFields).fields(fields)
                .paging(pagination).sort(sorting);

        // Request
        Response<ResultSetRepresentation<ResultNodeRepresentation>> response = client.getSearchAPI().searchCall(body)
                .execute();
        Assert.assertNotNull(response);
        Assert.assertEquals(response.isSuccessful(), true);

        // Check Response
        ResultSetRepresentation<ResultNodeRepresentation> resultSet = response.body();
        Assert.assertNotNull("Response is empty", resultSet);
        Assert.assertNotNull("Response has no entries  ", resultSet.getList());
        Assert.assertNotNull("Response has no Pagination Info", resultSet.getPagination());
        Assert.assertNotNull("Response has no Context", resultSet.getContext());

        // Check Pagination
        Assert.assertEquals(1, resultSet.getPagination().getSkipCount());
        Assert.assertEquals(5, resultSet.getPagination().getCount());
        Assert.assertTrue(resultSet.getPagination().getHasMoreItems());
        Assert.assertEquals(5, resultSet.getList().size());

        // Check Context
        Assert.assertNull("Consistency is not empty", resultSet.getContext().getConsistency());
        Assert.assertNotNull("SpellCheck is null", resultSet.getContext().getSpellcheck());
        Assert.assertTrue("SpellCheck is not empty", resultSet.getContext().getSpellcheck().isEmpty());
        Assert.assertNotNull("Facet Fields is null", resultSet.getContext().getFacetFields());
        Assert.assertTrue("Facet Fields is not empty", resultSet.getContext().getFacetFields().isEmpty());
        Assert.assertNotNull("Facet Queryies is empty", resultSet.getContext().getFacetQueries());

        // Check Results (Filter Fields)
        List<ResultNodeRepresentation> results = resultSet.getList();
        for (ResultNodeRepresentation node : results)
        {
            // Filter fields
            Assert.assertNotNull(node.getId());
            Assert.assertNotNull(node.getName());
            Assert.assertNotNull(node.getSearch());
            Assert.assertNotNull(node.getSearch().getScore());

            // Others must be null
            Assert.assertNull(node.getCreatedAt());
            Assert.assertNull(node.getCreatedByUser());
            Assert.assertNull(node.getModifiedByUser());
            Assert.assertNull(node.getModifiedAt());
        }
    }

}
