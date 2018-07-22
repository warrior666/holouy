package com.alfresco.client.api.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.alfresco.client.api.tests.authentication.AuthenticationApiTest;
import com.alfresco.client.api.tests.core.NodesApiTest;

@RunWith(Suite.class)
@SuiteClasses({AuthenticationApiTest.class, NodesApiTest.class})
public class AllTests {

}
