package com.adaptris.utils;

import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author mwarman
 */
public class ProjectMigratorTest {

  @Test
  public void convertMap() throws Exception{
    Map<String, String> variables = new HashMap<>();
    variables.put("data", "value");
    Map<String, String> results = new ProjectMigrator().convert("<foo><foo1>Foo Test 1</foo1><services><another1><unique-id>value</unique-id><test1>Foo Test 2.0</test1><test1>${data}</test1></another1></services><foo3>Foo Test 3</foo3><foo4>Foo Test 4</foo4></foo>", variables);
    assertEquals(1, results.size());
    assertEquals("data", results.get("/foo/services/another1[unique-id=\"value\"]/test1[2]"));
  }

  @Test
  public void convertMapJMSExample() throws Exception {
    ProjectMigrator projectMigrator = new ProjectMigrator();
    File adpXml = new File(this.getClass().getClassLoader().getResource("adapter.xml").getFile());
    File vars = new File(this.getClass().getClassLoader().getResource("variables.properties").getFile());
    Map<String, String> results  = projectMigrator.convert(projectMigrator.readFile(adpXml.getAbsolutePath(), StandardCharsets.UTF_8), projectMigrator.loadProperties(vars.getAbsolutePath()));
    assertEquals(10, results.size());
    assertEquals("AMQ_USERNAME", results.get("/adapter/shared-components/connections/jms-connection[unique-id=\"jms-connection-AMQ\"]/user-name"));
    assertEquals("AMQ_HOST", results.get("/adapter/shared-components/connections/jms-connection[unique-id=\"jms-connection-AMQ\"]/vendor-implementation/broker-url"));
    assertEquals("SONIC_CONNECTION_WAIT", results.get("/adapter/shared-components/connections/jms-connection[unique-id=\"jms-connection-SonicMQ\"]/connection-retry-interval/interval"));
    assertEquals("SONIC_HOST", results.get("/adapter/shared-components/connections/jms-connection[unique-id=\"jms-connection-SonicMQ\"]/vendor-implementation/broker-url"));
    assertEquals("SONIC_USERNAME", results.get("/adapter/shared-components/connections/jms-connection[unique-id=\"jms-connection-SonicMQ\"]/user-name"));
    assertEquals("SONIC_DOMAIN", results.get("/adapter/shared-components/connections/jms-connection[unique-id=\"jms-connection-SonicMQ\"]/vendor-implementation/connect-id"));
    assertEquals("AMQ_PASSWORD", results.get("/adapter/shared-components/connections/jms-connection[unique-id=\"jms-connection-AMQ\"]/password"));
    assertEquals("SONIC_QUEUE", results.get("/adapter/channel-list/channel[unique-id=\"sleepy-murdock\"]/workflow-list/standard-workflow[unique-id=\"pedantic-lovelace\"]/producer/destination/destination"));
    assertEquals("SONIC_CLIENT_ID", results.get("/adapter/shared-components/connections/jms-connection[unique-id=\"jms-connection-SonicMQ\"]/client-id"));
    assertEquals("SONIC_PASSWORD", results.get("/adapter/shared-components/connections/jms-connection[unique-id=\"jms-connection-SonicMQ\"]/password"));

  }
}