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

package com.alfresco.client.api.tests.core;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alfresco.client.api.AlfrescoAPITestCase;
import com.alfresco.client.api.common.representation.ResultPaging;
import com.alfresco.client.api.core.PeopleAPI;
import com.alfresco.client.api.core.SitesAPI;
import com.alfresco.client.api.core.model.body.SiteBodyCreate;
import com.alfresco.client.api.core.model.body.SiteMembershipRequestBodyCreate;
import com.alfresco.client.api.core.model.representation.PersonRepresentation;
import com.alfresco.client.api.core.model.representation.SiteContainerRepresentation;
import com.alfresco.client.api.core.model.representation.SiteMemberRepresentation;
import com.alfresco.client.api.core.model.representation.SiteMembershipRequestRepresentation;
import com.alfresco.client.api.core.model.representation.SiteRepresentation;
import com.alfresco.client.api.core.model.representation.SiteVisibilityEnum;

import retrofit2.Response;

public class SiteApiTest extends AlfrescoAPITestCase
{
    public static final String SWSDP_ID = "swsdp";

    @BeforeClass
    public void prepare() throws Exception
    {
        client = prepareClient(TEST_ENDPOINT, TEST_USERNAME, TEST_PASSWORD);
    }

    @Test
    public void siteApiTests() throws IOException
    {
        //
        SitesAPI siteService = client.getAPI(SitesAPI.class);

        // Request Site By Id
        Response<SiteRepresentation> response = siteService.getSiteCall(SWSDP_ID).execute();
        Assert.assertNotNull(response);
        Assert.assertEquals(response.isSuccessful(), true);

        // Check Response
        SiteRepresentation siteRepresentation = response.body();
        Assert.assertNotNull("Response is empty", siteRepresentation);
        Assert.assertEquals("Site Id seems wrong.", siteRepresentation.getId(), SWSDP_ID);
        Assert.assertEquals("Site description seems wrong.", siteRepresentation.getDescription(), "This is a Sample Alfresco Team site.");
        Assert.assertEquals("Site visibility seems wrong.", siteRepresentation.getVisibility(), "PUBLIC");
        Assert.assertEquals("Site title seems wrong.", siteRepresentation.getTitle(), "Sample: Web Site Design Project");
        Assert.assertEquals("Site guid seems wrong.", siteRepresentation.getGuid(), "b4cff62a-664d-4d45-9302-98723eac1319");
    }

    @Test
    public void siteContainersApiTests() throws IOException
    {
        SitesAPI siteService = client.getAPI(SitesAPI.class);

        // Request Site Containers
        Response<ResultPaging<SiteContainerRepresentation>> containersResponse = siteService
                .listSiteContainersCall(SWSDP_ID).execute();
        Assert.assertNotNull(containersResponse);
        Assert.assertEquals(containersResponse.isSuccessful(), true);

        // Check Response
        ResultPaging<SiteContainerRepresentation> siteContainerResponse = containersResponse.body();
        Assert.assertNotNull("Response is empty", siteContainerResponse);
        Assert.assertNotNull("Response has no data", siteContainerResponse.getList());
        Assert.assertNotNull("Response has no pagination", siteContainerResponse.getPagination());
        Assert.assertEquals("Pagination count is wrong.", siteContainerResponse.getPagination().getCount(), 5);
        Assert.assertFalse("Pagination HasMoreItem is wrong.", siteContainerResponse.getPagination().getHasMoreItems());
        Assert.assertEquals("Pagination SkipCount is wrong.", siteContainerResponse.getPagination().getSkipCount(), 0);
        Assert.assertEquals("Pagination TotalItem is wrong.", siteContainerResponse.getPagination().getTotalItems(), 5);

        // Loop over Container Information
        for (int i = 0; i < siteContainerResponse.getPagination().getCount(); i++)
        {
            Assert.assertNotNull("Container ID is empty", siteContainerResponse.getList().get(i).getId());
            Assert.assertNotNull("Container Folder ID is empty", siteContainerResponse.getList().get(i).getFolderId());
        }
    }

    public static final String SITE_TEST_ID = "AClientSiteTest";

    public static final String SITE_TEST_DESCRIPTION = "ClientSiteTest";

    public static final String SITE_TEST_TITLE = "ClientSiteTest";

    public static final SiteVisibilityEnum SITE_TEST_VISIBILITY = SiteVisibilityEnum.PUBLIC;

    @Test
    public void siteLifecyleTest() throws IOException, InterruptedException
    {
        SitesAPI siteService = client.getSitesAPI();

        // Check Test site is not present
        Response<SiteRepresentation> siteResponse = siteService.getSiteCall(SITE_TEST_ID).execute();
        if (siteResponse.isSuccessful())
        {
            // Delete Site
            siteService.deleteSiteCall(SITE_TEST_ID, true).execute();
            for (int i = 0; i < 5; i++)
            {
                // Deleting site might takes time so we have to wait few
                // moments
                Thread.sleep(5000);
                // Retrieve by its Id
                siteResponse = siteService.getSiteCall(SITE_TEST_ID).execute();
                if (!siteResponse.isSuccessful())
                {
                    break;
                }

                if (i == 5)
                {
                    Assert.fail("Unable to delete site even after waiting");
                }
            }
        }
        else
        {
            // Check Error
            Assert.assertFalse(siteResponse.isSuccessful());
            Assert.assertNotNull(siteResponse.errorBody());
        }

        // Create Test Site
        siteResponse = siteService
                .createSiteCall(
                        new SiteBodyCreate(SITE_TEST_ID, SITE_TEST_TITLE, SITE_TEST_DESCRIPTION, SITE_TEST_VISIBILITY))
                .execute();
        Assert.assertTrue(siteResponse.isSuccessful());
        Assert.assertNotNull(siteResponse.body());
        SiteRepresentation siteRepresentation = siteResponse.body();

        Assert.assertEquals(siteRepresentation.getId(), SITE_TEST_ID, "Site Id seems wrong.");
        Assert.assertEquals(siteRepresentation.getDescription(), SITE_TEST_DESCRIPTION,
                "Site description seems wrong.");
        Assert.assertEquals(siteRepresentation.getVisibility(), SITE_TEST_VISIBILITY.toString(),
                "Site visibility seems wrong.");
        Assert.assertEquals("Site visibility seems wrong.", siteRepresentation.getVisibilityEnum(), SiteVisibilityEnum.PUBLIC);
        Assert.assertEquals("Site title seems wrong.", siteRepresentation.getTitle(), SITE_TEST_TITLE);
        Assert.assertNotNull("Site guid seems wrong.", siteRepresentation.getGuid());

        // Delete Site
        // Delete Site
        siteService.deleteSiteCall(SITE_TEST_ID, true).execute();
        for (int i = 0; i < 5; i++)
        {
            // Deleting site might takes time so we have to wait few
            // moments
            Thread.sleep(5000);
            // Retrieve by its Id
            siteResponse = siteService.getSiteCall(SITE_TEST_ID).execute();
            if (!siteResponse.isSuccessful()) { return; }
        }
        Assert.fail("Unable to delete site even after waiting");
    }

    @Test
    public void siteMembersApiTests() throws IOException
    {
        //
        SitesAPI siteService = client.getAPI(SitesAPI.class);

        // Request Site By Id
        Response<ResultPaging<SiteMemberRepresentation>> response = siteService.listSiteMembershipsCall(SWSDP_ID)
                .execute();
        Assert.assertNotNull(response);
        Assert.assertEquals(response.isSuccessful(), true);

        // Check Response
        ResultPaging<SiteMemberRepresentation> membersResponse = response.body();
        Assert.assertNotNull("Response is empty", membersResponse);
        Assert.assertNotNull("Response has no data", membersResponse.getList());
        Assert.assertNotNull("Response has no pagination", membersResponse.getPagination());
        Assert.assertEquals("Pagination count is wrong.", membersResponse.getPagination().getCount(), 3);
        Assert.assertFalse("Pagination HasMoreItem is wrong.", membersResponse.getPagination().getHasMoreItems());
        Assert.assertEquals("Pagination SkipCount is wrong.", membersResponse.getPagination().getSkipCount(), 0);

        // Loop over Container Information
        for (SiteMemberRepresentation member : membersResponse.getList())
        {
            Assert.assertNotNull("Member ID is empty", member.getId());
            Assert.assertNotNull("Member Role is empty", member.getRole());
            Assert.assertNotNull("Member PersonInfo is empty", member.getPerson());

            PersonRepresentation person = member.getPerson();
            Assert.assertNotNull("Person Firstname is empty", person.getFirstName());
            // Assert.assertNotNull(person.getLastName(), "Person LastName is
            // empty");
            Assert.assertNotNull("Person Email is empty", person.getEmail());
            // Assert.assertTrue(person.isEnabled(), "Person IsEnabled is
            // False");
            Assert.assertTrue( "Person EmailNotificationsEnabled is False", person.isEmailNotificationsEnabled());

            // Request One Member Info
            Response<SiteMemberRepresentation> memberInfoResponse = siteService
                    .getSiteMembershipCall(SWSDP_ID, person.getId()).execute();
            SiteMemberRepresentation memberInfo = memberInfoResponse.body();
            PersonRepresentation personInfo = memberInfo.getPerson();

            // Member Info Comparison
            Assert.assertEquals(member.getId(), memberInfo.getId());
            Assert.assertEquals(member.getRole(), memberInfo.getRole());

            // Person Info Comparison
            Assert.assertEquals(person.getId(), personInfo.getId());
            Assert.assertEquals(person.getFirstName(), personInfo.getFirstName());
            Assert.assertEquals(person.getLastName(), personInfo.getLastName());
            Assert.assertEquals(person.getEmail(), personInfo.getEmail());
        }
    }

    @Test
    public void retrieveAllSitesTest() throws IOException
    {
        // Request All Sites
        Response<ResultPaging<SiteRepresentation>> response = client.getAPI(SitesAPI.class).listSitesCall().execute();
        Assert.assertNotNull(response);
        Assert.assertEquals(response.isSuccessful(), true);

        // Check Response
        ResultPaging<SiteRepresentation> sitesResponse = response.body();
        Assert.assertNotNull("Response is empty", sitesResponse);
        Assert.assertNotNull("Response has no listActivitiesForPersonCall of sites", sitesResponse.getList());

        // Check Pagination & Entries
        List<SiteRepresentation> sitesRepresentation = sitesResponse.getList();
        // Assert.assertNotNull(sitesRepresentation.getEntries(), "Response has
        // no listActivitiesForPersonCall of sites");
        Assert.assertNotNull("Response has no pagination", sitesResponse.getPagination());

        // Retrieve SWSDP Site and check we have the information
        for (int i = 0; i < sitesResponse.getPagination().getCount(); i++)
        {
            SiteRepresentation siteRepresentation = sitesRepresentation.get(i);
            if (SWSDP_ID.equals(siteRepresentation.getId()))
            {
                Assert.assertEquals("Site Id seems wrong.", siteRepresentation.getId(), SWSDP_ID);
                Assert.assertEquals("Site description seems wrong.", siteRepresentation.getDescription(), "This is a Sample Alfresco Team site.");
                Assert.assertEquals("Site visibility seems wrong.", siteRepresentation.getVisibility(), "PUBLIC");
                Assert.assertEquals("Site visibility seems wrong.", siteRepresentation.getVisibilityEnum(), SiteVisibilityEnum.PUBLIC);
                Assert.assertEquals("Site title seems wrong.", siteRepresentation.getTitle(), "Sample: Web Site Design Project");
                Assert.assertEquals("Site guid seems wrong.", siteRepresentation.getGuid(), "b4cff62a-664d-4d45-9302-98723eac1319");
            }
        }
    }

    @Test
    public void siteMemberships() throws IOException
    {
        PeopleAPI peopleAPI = client.getPeopleAPI();
        SitesAPI siteAPI = client.getSitesAPI();

        // LIST MEMBERSHIP REQUEST
        Response<ResultPaging<SiteMembershipRequestRepresentation>> response = siteAPI
                .listSiteMembershipRequestsForPersonCall(TEST_USERNAME).execute();
        Assert.assertNotNull(response);
        Assert.assertEquals(response.isSuccessful(), true);

        // Check Response
        ResultPaging<SiteMembershipRequestRepresentation> siteMembership = response.body();
        Assert.assertNotNull("Response is empty", siteMembership);
        Assert.assertNotNull("Response has no listActivitiesForPersonCall of body", siteMembership.getList());
        Assert.assertEquals(siteMembership.getPagination().getCount(), 0);

        // CREATE MEMBERSHIP REQUEST
        SiteMembershipRequestBodyCreate request = new SiteMembershipRequestBodyCreate("moderatedsite", "Name", "Title");
        Response<SiteMembershipRequestRepresentation> respM = siteAPI
                .createSiteMembershipRequestForPersonCall(TEST_USERNAME, request, null).execute();
        Assert.assertNotNull(response);
        Assert.assertEquals(response.isSuccessful(), true);

        // Check Response
        SiteMembershipRequestRepresentation siteMembershipR = respM.body();
        Assert.assertNotNull("Response is empty", siteMembershipR);
        Assert.assertNotNull("Response has no Id", siteMembershipR.getId());
        Assert.assertNotNull("Response has no Site", siteMembershipR.getSite());
        Assert.assertEquals("Wrong site", siteMembershipR.getSite().getId(), "moderatedsite");
        Assert.assertNotNull("Response has no Creation Date", siteMembershipR.getCreatedAt());

        // LIST MEMBERSHIP REQUEST
        response = siteAPI.listSiteMembershipRequestsForPersonCall(TEST_USERNAME).execute();
        Assert.assertEquals(response.isSuccessful(), true);
        Assert.assertEquals(response.body().getPagination().getCount(), 1);

        // REMOVE MEMBERSHIP REQUEST
        Response<Void> responseRemove = siteAPI.deleteSiteMembershipRequestCall(TEST_USERNAME, "moderatedsite")
                .execute();
        Assert.assertEquals(responseRemove.isSuccessful(), true);
    }
}
