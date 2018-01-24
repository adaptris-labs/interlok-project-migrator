package com.adaptris.utils;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * @author mwarman
 */
public class XmlUtilsTest {

  @Test
  public void resolveXincludes() throws Exception {
    File file = new File(this.getClass().getClassLoader().getResource("adapter.xml").getFile());
    String xml = "<xi:include xmlns:xi=\"http://www.w3.org/2001/XInclude\" href=\"file:///" + file.getAbsolutePath()  +"\"/>";
    String result = XmlUtils.resolveXincludes(xml);
    assertFalse(result.contains("xi:include"));
    assertTrue(result.contains("adapter"));
  }
}