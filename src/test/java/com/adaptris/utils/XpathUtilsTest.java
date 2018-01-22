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
    List<String> s = XpathUtils.getXPath("<foo><foo1>Foo Test 1</foo1><foo2><another1><unique-id>value</unique-id><test1>Foo Test 2.0</test1><test1>${data}</test1></another1></foo2><foo3>Foo Test 3</foo3><foo4>Foo Test 4</foo4></foo>"
        , "${data}");
    assertEquals(1, s.size());
    assertEquals("/foo/foo2/another1[unique-id=\"value\"]/test1[2]", s.get(0));
  }

  @Test
  public void getXPathTwoResults() throws Exception {
    List<String> s = XpathUtils.getXPath("<foo><foo1>Foo Test 1</foo1><foo2><another1><unique-id>value</unique-id><test1>Foo Test 2.0</test1><test1>${data}</test1></another1></foo2><foo3>Foo Test 3</foo3><foo4>${data}</foo4></foo>"
        , "${data}");
    assertEquals(2, s.size());
    assertTrue(s.contains("/foo/foo2/another1[unique-id=\"value\"]/test1[2]"));
    assertTrue(s.contains("/foo/foo4"));
  }

}