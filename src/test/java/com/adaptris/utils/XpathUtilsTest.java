package com.adaptris.utils;

import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * @author mwarman
 */
public class XpathUtilsTest {

  @Test
  public void getXPath() throws Exception {
    List<String> s = XpathUtils.getXPath(XpathUtils.createDocument("<foo><foo1>Foo Test 1</foo1><services><another1><unique-id>value</unique-id><test1>Foo Test 2.0</test1><test1>${data}</test1></another1></services><foo3>Foo Test 3</foo3><foo4>Foo Test 4</foo4></foo>")
      ,"${data}"
      ,1);
    assertEquals(1, s.size());
    assertEquals("/foo/services/another1[unique-id=\"value\"]/test1[2]", s.get(0));
  }

  @Test
  public void getXPathTwoResults() throws Exception {
    List<String> s = XpathUtils.getXPath(XpathUtils.createDocument("<foo><foo1>Foo Test 1</foo1><services><another1><unique-id>value</unique-id><test1>Foo Test 2.0</test1><test1>${data}</test1></another1></services><foo3>Foo Test 3</foo3><foo4>${data}</foo4></foo>")
      , "${data}"
      ,2);
    assertEquals(2, s.size());
    assertTrue(s.contains("/foo/services/another1[unique-id=\"value\"]/test1[2]"));
    assertTrue(s.contains("/foo/foo4"));
  }

}