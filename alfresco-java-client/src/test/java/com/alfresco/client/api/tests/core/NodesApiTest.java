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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alfresco.client.api.AlfrescoAPITestCase;
import com.alfresco.client.api.common.representation.ResultPaging;
import com.alfresco.client.api.core.NodesAPI;
import com.alfresco.client.api.core.model.body.AssociationBody;
import com.alfresco.client.api.core.model.body.ChildAssociationBody;
import com.alfresco.client.api.core.model.body.NodeBodyCopy;
import com.alfresco.client.api.core.model.body.NodeBodyCreate;
import com.alfresco.client.api.core.model.body.NodeBodyLock;
import com.alfresco.client.api.core.model.body.NodeBodyUpdate;
import com.alfresco.client.api.core.model.representation.AssociationInfoRepresentation;
import com.alfresco.client.api.core.model.representation.NodeRepresentation;
import com.alfresco.client.api.tests.utils.IOUtils;
import com.alfresco.client.api.tests.utils.NodeRefUtils;
import com.google.gson.internal.LinkedTreeMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class NodesApiTest extends AlfrescoAPITestCase
{
    // ///////////////////////////////////////////////////////////////////////////
    // CONSTANTS
    // ///////////////////////////////////////////////////////////////////////////
    public static final String USERID_SYSTEM = "System";

    public static final String USERID_ADMIN = "admin";

    public static final String USER_DISPLAY_ADMIN = "Administrator";

    public static final ArrayList<String> DEFAULT_FOLDER_ASPECTS = new ArrayList<String>(3)
    {
        {
            add("cm:titled");
            add("cm:auditable");
            add("app:uifacets");
        }
    };

    public static final ArrayList<String> DEFAULT_FILE_ASPECTS = new ArrayList<String>(3)
    {
        {
            add("cm:titled");
            add("cm:auditable");
            add("app:uifacets");
            add("cm:versionable");
            add("cm:author");
        }
    };

    // ///////////////////////////////////////////////////////////////////////////
    // TESTS PREPARATION
    // ///////////////////////////////////////////////////////////////////////////
    @BeforeClass
    public void prepare() throws Exception
    {
        // Start from a vanilla server installation
        client = prepareClient(TEST_ENDPOINT, TEST_USERNAME, TEST_PASSWORD);
    }

    @Before
    public void createLocalTmpFolder()
    {
        try
        {
            tmpFolder = Files.createTempDirectory("AlfrescoApiTests").toFile();
            tmpFolder.deleteOnExit();
        }
        catch (Exception e)
        {

        }
    }

    @After
    public void cleanLocalTmpFolder()
    {
        tmpFolder.delete();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // TESTS
    // ///////////////////////////////////////////////////////////////////////////
    @Test
    public void nodeAPITests() throws IOException
    {
        // Create Node API
        NodesAPI nodeService = client.getAPI(NodesAPI.class);

        // Request Node By Id
        Response<NodeRepresentation> response = nodeService.getNodeCall(NodesAPI.FOLDER_ROOT).execute();
        Assert.assertNotNull(response);
        Assert.assertEquals(response.isSuccessful(), true);

        // Check Response
        NodeRepresentation nodeResponse = response.body();
        Assert.assertNotNull("Response is empty", nodeResponse);
        Assert.assertNotNull("Response has no data", nodeResponse);

        // Check Node Info
        NodeRepresentation rootNode = nodeResponse;
        Assert.assertTrue("Root Folder is not a Folder ??", rootNode.isFolder());
        Assert.assertEquals("Root Folder is not Company Home", rootNode.getName(), "Company Home");
        Assert.assertEquals("Root Folder is not a Folder ??", rootNode.getNodeType(), "cm:folder");
        Assert.assertNotNull("Node Identifier is empty", rootNode.getId());
        Assert.assertTrue("Node Identifier is not an identifier", NodeRefUtils.isIdentifier(rootNode.getId()));
        Assert.assertNull("Company Home has a Parent ?", rootNode.getParentId());

        // Aspects
        Assert.assertFalse("Root Folder is not a Folder ??", rootNode.getAspects().isEmpty());
        Assert.assertEquals("Root folder has more aspects", rootNode.getAspects().size(), 3);
        for (String aspect : rootNode.getAspects())
        {
            Assert.assertTrue(aspect + "is not a default aspect", DEFAULT_FOLDER_ASPECTS.contains(aspect));
        }

        // CreatedAt & ModifiedAt
        Assert.assertNotNull("createdAt is empty", rootNode.getCreatedAt());
        Assert.assertNotNull("modifiedAt is empty", rootNode.getModifiedAt());

        // createdByUser & modifiedByUser
        Assert.assertNotNull("createdByUser is empty", rootNode.getCreatedByUser());
        Assert.assertNotNull("modifiedByUser is empty", rootNode.getModifiedByUser());
        if (USERID_SYSTEM.equals(rootNode.getModifiedByUser().getDisplayName()))
        {
            Assert.assertEquals(rootNode.getModifiedByUser().getDisplayName(), USERID_SYSTEM);
            Assert.assertEquals(rootNode.getModifiedByUser().getId(), null);
            Assert.assertEquals(rootNode.getCreatedByUser().getId(), null);
            Assert.assertEquals(rootNode.getCreatedByUser().getDisplayName(), USERID_SYSTEM);
        }
        else
        {
            Assert.assertEquals(rootNode.getModifiedByUser().getDisplayName(), USER_DISPLAY_ADMIN);
            Assert.assertEquals(rootNode.getModifiedByUser().getId(), USERID_ADMIN);
            Assert.assertEquals(rootNode.getCreatedByUser().getId(), USERID_SYSTEM);
            Assert.assertEquals(rootNode.getCreatedByUser().getDisplayName(), USERID_SYSTEM);
        }

        // Properties
        Assert.assertNotNull("Properties is empty", rootNode.getProperties());
        Assert.assertEquals("Wrong value for cm:title", rootNode.getProperties().get("cm:title"), "Company Home");

    }

    @Test
    public void folderLifeCycleTests() throws IOException
    {
        // Request Node By Id
        NodesAPI nodeService = client.getAPI(NodesAPI.class);

        // Request Site By Id
        Response<ResultPaging<NodeRepresentation>> response = nodeService.listNodeChildrenCall(NodesAPI.FOLDER_MY)
                .execute();
        Assert.assertNotNull(response);
        Assert.assertEquals(response.isSuccessful(), true);

        // Check Response
        ResultPaging<NodeRepresentation> nodesResponse = response.body();
        Assert.assertNotNull("Response is empty", nodesResponse);
        Assert.assertNotNull("Response has no listActivitiesForPersonCall object", nodesResponse.getList());
        // Assert.assertNotNull(nodesResponse.getList().getPagination(),
        // "Response has no pagination");

        // Clean Up if necessary
        if (nodesResponse.getCount() != 7)
        {
            for (int i = 0; i < nodesResponse.getCount(); i++)
            {
                String name = nodesResponse.getList().get(i).getName();
                if (name.equals("TestV3") || name.equals("TestV2") || name.equals("Test"))
                {
                    nodeService.deleteNodeCall(nodesResponse.getList().get(i).getId()).execute();
                }
            }
            nodesResponse = nodeService.listNodeChildrenCall(NodesAPI.FOLDER_MY).execute().body();
        }

        Assert.assertEquals("Pagination Total items is wrong. Stale state ?", nodesResponse.getCount(), 7);
        // Assert.assertEquals(nodesResponse.getCount(), 7,
        // "Pagination Count is wrong. Stale state ?");

        Assert.assertNotNull("Response has no pagination", nodesResponse.getList().get(0));

        // CREATE
        NodeBodyCreate request = new NodeBodyCreate("Test", "cm:folder");
        Response<NodeRepresentation> testNodeResponse = nodeService.createNodeCall(NodesAPI.FOLDER_ROOT, request)
                .execute();
        Assert.assertNotNull("Response is empty", testNodeResponse);
        Assert.assertEquals(testNodeResponse.isSuccessful(), true);
        Assert.assertNotNull("Response body is empty", testNodeResponse.body());

        // Check Node Info
        NodeRepresentation testNode = testNodeResponse.body();
        Assert.assertTrue("Test Folder is not a Folder ??", testNode.isFolder());
        Assert.assertEquals("Root Folder is not Company Home", testNode.getName(), "Test");
        Assert.assertEquals("Root Folder is not a Folder ??", testNode.getNodeType(), "cm:folder");
        Assert.assertNotNull("Node Identifier is empty", testNode.getId());
        Assert.assertTrue("Node Identifier is not an identifier", NodeRefUtils.isIdentifier(testNode.getId()));
        Assert.assertNotNull("Test has a Parent ?", testNode.getParentId());
        for (String aspect : testNode.getAspects())
        {
            Assert.assertTrue(aspect + "is not a default aspect", DEFAULT_FOLDER_ASPECTS.contains(aspect));
        }

        Assert.assertFalse("Titled has been set without requesting", testNode.hasAspects("cm:titled"));
        Assert.assertNull("Properties is defined", testNode.getProperties());

        // RENAME
        NodeBodyUpdate renameRequest = new NodeBodyUpdate("TestV2");
        Response<NodeRepresentation> updatedResponse = nodeService.updateNodeCall(testNode.getId(), renameRequest)
                .execute();
        Assert.assertEquals("Folder renaming is wrong", updatedResponse.body().getName(), "TestV2");

        Response<NodeRepresentation> updated2Response = nodeService.getNodeCall(testNode.getId()).execute();
        Assert.assertEquals("Folder renaming is wrong", updated2Response.body().getName(), "TestV2");
        Assert.assertEquals("Folder renaming is wrong", updated2Response.body().getName(), updatedResponse.body().getName());

        // EDIT PROPERTIES
        LinkedTreeMap<String, Object> properties = new LinkedTreeMap<>();
        properties.put("cm:title", "ALFRESCO");
        NodeBodyUpdate editRequest = new NodeBodyUpdate("TestV3", null, properties, null);
        Response<NodeRepresentation> editedResponse = nodeService.updateNodeCall(testNode.getId(), editRequest)
                .execute();

        Assert.assertEquals("Wrong value for cm:title", editedResponse.body().getProperties().get("cm:title"), "ALFRESCO");
        Assert.assertNull("Wrong value for cm:description", editedResponse.body().getProperties().get("cm:description"));

        // DELETE
        Response<Void> deleteResponse = nodeService.deleteNodeCall(testNode.getId()).execute();
        Assert.assertNotNull("Response is empty", deleteResponse);
        Assert.assertEquals(deleteResponse.isSuccessful(), true);
        Assert.assertNull("Response body is not empty", deleteResponse.body());
    }

    @Test
    public void ContentLifeCycleTests() throws IOException
    {
        NodesAPI nodeService = client.getAPI(NodesAPI.class);

        // Clean Up if necessary
        Response<ResultPaging<NodeRepresentation>> response = nodeService.listNodeChildrenCall(NodesAPI.FOLDER_MY)
                .execute();
        ResultPaging<NodeRepresentation> nodesResponse = response.body();
        if (nodesResponse.getCount() != 7)
        {
            for (int i = 0; i < nodesResponse.getCount(); i++)
            {
                String name = nodesResponse.getList().get(i).getName();
                if (name.equals("test.txt"))
                {
                    nodeService.deleteNodeCall(nodesResponse.getList().get(i).getId()).execute();
                }
            }
        }

        // Check if test file is present
        checkResourceFile("/com/alfresco/client/api/tests/test.txt");

        // Create Body
        File file = getResourceFile("/com/alfresco/client/api/tests/test.txt");
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), file);
        MultipartBody.Builder multipartBuilder = new MultipartBody.Builder();
        multipartBuilder.addFormDataPart("filename", "test.txt", requestBody);
        RequestBody fileRequestBody = multipartBuilder.build();

        // Let's Create
        Response<NodeRepresentation> createdNodeResponse = nodeService
                .createNodeCall(NodesAPI.FOLDER_MY, fileRequestBody).execute();

        // Check Result
        Assert.assertNotNull(createdNodeResponse);
        Assert.assertEquals("Upload doesn't work", createdNodeResponse.isSuccessful(), true);

        // Check Response
        NodeRepresentation testNode = createdNodeResponse.body();
        Assert.assertNotNull("Response is empty", testNode);
        Assert.assertFalse("Test Content is a Folder ??", testNode.isFolder());
        Assert.assertTrue("Test Content is not a Content ??", testNode.isFile());
        Assert.assertEquals("Root Folder is not Company Home", testNode.getName(), "test.txt");
        Assert.assertEquals("Root Folder is not a Folder ??", testNode.getNodeType(), "cm:content");
        Assert.assertNotNull("Node Identifier is empty", testNode.getId());
        Assert.assertTrue("Node Identifier is not an identifier", NodeRefUtils.isIdentifier(testNode.getId()));
        Assert.assertNotNull("Test has a Parent ?", testNode.getParentId());
        for (String aspect : testNode.getAspects())
        {
            Assert.assertTrue(aspect + " is not a default aspect", DEFAULT_FILE_ASPECTS.contains(aspect));
        }

        Assert.assertNotNull("Test has a Parent ?", testNode.getParentId());

        Assert.assertNotNull("Content is empty.", testNode.getContent());
        Assert.assertEquals("Content Size is wrong", testNode.getContent().getSizeInBytes(), 12);
        Assert.assertEquals("Content Mimetype is wrong", testNode.getContent().getMimeType(), "text/plain");
        Assert.assertEquals("Content MimeType Name is false", testNode.getContent().getMimeTypeName(), "Plain Text");

        // Download Content
        Call<ResponseBody> downloadCall = nodeService.getNodeContentCall(testNode.getId());
        File dlFile = new File(tmpFolder, "dl.txt");
        IOUtils.copyFile(downloadCall.execute().body().byteStream(), dlFile);

        Assert.assertTrue(dlFile.exists());
        Assert.assertEquals(12, dlFile.length());

        // Create Body
        checkResourceFile("/com/alfresco/client/api/tests/test2.txt");
        file = getResourceFile("/com/alfresco/client/api/tests/test2.txt");
        requestBody = RequestBody.create(MediaType.parse("text/plain"), file);

        // Let's createCommentsCall
        Response<NodeRepresentation> updateNodeResponse = nodeService
                .updateNodeContentCall(testNode.getId(), requestBody).execute();
        // Check Result
        Assert.assertNotNull(updateNodeResponse);
        Assert.assertEquals("Update doesn't work", updateNodeResponse.isSuccessful(), true);

        // Check Response
        NodeRepresentation nodeUpdatedResponse = updateNodeResponse.body();
        Assert.assertNotNull("Response is empty", nodeUpdatedResponse);
        Assert.assertNotNull("Response has no data", nodeUpdatedResponse.getName());
        Assert.assertEquals("Content Size is wrong", nodeUpdatedResponse.getContent().getSizeInBytes(), 30);
        Assert.assertEquals("Content Mimetype is wrong", nodeUpdatedResponse.getContent().getMimeType(), "text/plain");
        Assert.assertEquals("Content MimeType Name is false", nodeUpdatedResponse.getContent().getMimeTypeName(), "Plain Text");

        // COPY
        NodeBodyCreate request = new NodeBodyCreate("Test", "cm:folder");
        NodeRepresentation testFolder = nodeService.createNodeCall(NodesAPI.FOLDER_ROOT, request).execute().body();

        NodeBodyCopy copyRequest = new NodeBodyCopy(testFolder.getId(), "Copied.txt");
        Response<NodeRepresentation> copiedNodeResponse = nodeService
                .copyNodeCall(nodeUpdatedResponse.getId(), copyRequest).execute();

        // Check Result
        Assert.assertNotNull(copiedNodeResponse);
        Assert.assertEquals("Update doesn't work", copiedNodeResponse.isSuccessful(), true);

        // Check Response
        NodeRepresentation copiedNode = copiedNodeResponse.body();
        Assert.assertNotNull("Response is empty", copiedNode);
        Assert.assertEquals("Copied.txt", copiedNode.getName());
        Assert.assertEquals(copiedNode.getParentId(), testFolder.getId());

        // DELETE
        Response<Void> deleteResponse = nodeService.deleteNodeCall(testNode.getId()).execute();
        Assert.assertTrue("Deletion has failed", deleteResponse.isSuccessful());

        // MOVE
        NodeRepresentation rootFolder = nodeService.getNodeCall(NodesAPI.FOLDER_ROOT).execute().body();
        copyRequest = new NodeBodyCopy(rootFolder.getId(), "MovedText.txt");
        Response<NodeRepresentation> movedNodeResponse = nodeService.moveNodeCall(copiedNode.getId(), copyRequest)
                .execute();

        // Check Result
        Assert.assertNotNull(movedNodeResponse);
        Assert.assertEquals("Update doesn't work", movedNodeResponse.isSuccessful(), true);

        // Check Response
        NodeRepresentation movedNode = movedNodeResponse.body();
        Assert.assertNotNull("Response is empty", movedNode);
        Assert.assertEquals("MovedText.txt", movedNode.getName());
        Assert.assertEquals(movedNode.getParentId(), rootFolder.getId());

        // DELETE
        nodeService.deleteNodeCall(testFolder.getId()).execute();
        nodeService.deleteNodeCall(movedNode.getId()).execute();

    }

    public static final String CM_ORIGINAL = "cm:original";

    @Test
    public void nodeAssociationsTest() throws IOException
    {

        NodesAPI nodeService = client.getNodesAPI();

        // Create a file
        NodeRepresentation originalNode = createDummyFile();

        // Create a copy
        NodeRepresentation copiedNode = nodeService
                .copyNodeCall(originalNode.getId(), new NodeBodyCopy(originalNode.getParentId(), "DummyCopy.txt"))
                .execute().body();

        // CopiedNode contains a target association
        ResultPaging<NodeRepresentation> targetAssocNodes = nodeService.listTargetAssociationsCall(copiedNode.getId())
                .execute().body();
        Assert.assertEquals("Copied Association is not present after copy", targetAssocNodes.getCount(), 1);
        AssociationInfoRepresentation association = targetAssocNodes.getList().get(0).getAssociation();
        Assert.assertNotNull(association);
        Assert.assertEquals("AssocType is not cm:original", association.getAssocType(), CM_ORIGINAL);

        // dummyNode contains source association
        ResultPaging<NodeRepresentation> sourceAssocNodes = nodeService.listSourceAssociationsCall(originalNode.getId())
                .execute().body();
        Assert.assertEquals("Copied Association is not present after copy", sourceAssocNodes.getCount(), 1);
        AssociationInfoRepresentation sourceAssociation = sourceAssocNodes.getList().get(0).getAssociation();
        Assert.assertNotNull(sourceAssociation);
        Assert.assertEquals("AssocType is not cm:original", sourceAssociation.getAssocType(), CM_ORIGINAL);

        // Remove Association
        nodeService.deleteAssocationCall(copiedNode.getId(), originalNode.getId(), CM_ORIGINAL).execute();

        targetAssocNodes = nodeService.listTargetAssociationsCall(copiedNode.getId()).execute().body();
        Assert.assertEquals("Copied Association is still present after copy", targetAssocNodes.getCount(), 0);
        sourceAssocNodes = nodeService.listSourceAssociationsCall(originalNode.getId()).execute().body();
        Assert.assertEquals("Copied Association is still present after copy", sourceAssocNodes.getCount(), 0);

        // Recreate Association
        nodeService
                .createAssocationCall(copiedNode.getId(), new AssociationBody(originalNode.getId(), CM_ORIGINAL), null)
                .execute();

        targetAssocNodes = nodeService.listTargetAssociationsCall(copiedNode.getId()).execute().body();
        Assert.assertEquals("Copied Association is still present after copy", targetAssocNodes.getCount(), 1);
        sourceAssocNodes = nodeService.listSourceAssociationsCall(originalNode.getId()).execute().body();
        Assert.assertEquals("Copied Association is still present after copy", sourceAssocNodes.getCount(), 1);

        // Secondary Children
        // Create a folder and add it as second parent for copied Node
        NodeRepresentation parentNode2 = nodeService
                .createNodeCall(originalNode.getParentId(), new NodeBodyCreate("T", "cm:folder")).execute().body();
        ResultPaging<NodeRepresentation> childrenNodes = nodeService.listNodeChildrenCall(parentNode2.getId()).execute()
                .body();
        Assert.assertEquals("Wrong number of children for newly created folder", childrenNodes.getCount(), 0);

        nodeService.createSecondaryChildAssocationCall(parentNode2.getId(),
                new ChildAssociationBody(copiedNode.getId(), "cm:contains"), null).execute();

        // List children as usual
        childrenNodes = nodeService.listNodeChildrenCall(parentNode2.getId()).execute().body();
        Assert.assertEquals("Wrong number of children after adding a node as second children", childrenNodes.getCount(), 1);
        Assert.assertEquals( "CopiedId is not the node expected", childrenNodes.getList().get(0).getId(), copiedNode.getId());

        // List secondary children
        ResultPaging<NodeRepresentation> secondaryChildrenNodes = nodeService
                .listSecondaryChildrenCall(parentNode2.getId()).execute().body();
        Assert.assertEquals("Wrong number of children after adding a node as second children", secondaryChildrenNodes.getCount(), 1);
        Assert.assertEquals("CopiedId is not the node expected", secondaryChildrenNodes.getList().get(0).getId(), copiedNode.getId());

        // List parents
        ResultPaging<NodeRepresentation> parentCopiedNode = nodeService.listParentsCall(copiedNode.getId()).execute()
                .body();
        Assert.assertEquals("Wrong number of parents. It should be 2 (primary & secondary)", parentCopiedNode.getCount(), 2);

        for (NodeRepresentation parent : parentCopiedNode.getList())
        {
            Assert.assertNotNull(parent);
            Assert.assertTrue(
                    originalNode.getParentId().equals(parent.getId()) || parentNode2.getId().equals(parent.getId()));
        }

        nodeService.deleteSecondaryChildAssocationCall(parentNode2.getId(), copiedNode.getId()).execute();

        // List parents
        parentCopiedNode = nodeService.listParentsCall(copiedNode.getId()).execute().body();
        Assert.assertEquals("Wrong number of parents. It should be 1 (primary only)", parentCopiedNode.getCount(), 1);

        for (NodeRepresentation parent : parentCopiedNode.getList())
        {
            Assert.assertNotNull(parent);
            Assert.assertTrue(originalNode.getParentId().equals(parent.getId()));
        }
    }

    @Test
    public void nodeLockTests() throws IOException
    {

        NodesAPI nodeService = client.getNodesAPI();

        // Create a file
        NodeRepresentation originalNode = createDummyFile();

        Response<NodeRepresentation> response = nodeService.lockNodeCall(originalNode.getId(),
                new NodeBodyLock(0, NodeBodyLock.TypeEnum.ALLOW_OWNER_CHANGES, NodeBodyLock.LifetimeEnum.EPHEMERAL))
                .execute();

        // TODO

    }

    // ///////////////////////////////////////////////////////////////////////////
    // COPY
    // ///////////////////////////////////////////////////////////////////////////

}
