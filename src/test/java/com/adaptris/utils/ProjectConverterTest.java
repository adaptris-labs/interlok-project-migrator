package com.adaptris.utils;

import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author mwarman
 */
public class ProjectConverterTest {

  @Test
  public void convertMap() throws Exception{
    Map<String, String> variables = new HashMap<>();
    variables.put("data", "value");
    Map<String, String> results = new ProjectConverter().convert("<foo><foo1>Foo Test 1</foo1><foo2><another1><unique-id>value</unique-id><test1>Foo Test 2.0</test1><test1>${data}</test1></another1></foo2><foo3>Foo Test 3</foo3><foo4>Foo Test 4</foo4></foo>", variables);
    assertEquals(1, results.size());
    assertEquals("data", results.get("/foo/foo2/another1[unique-id=\"value\"]/test1[2]"));
  }
}