package com.adaptris.utils;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * @author mwarman
 */
public class XmlUtils {

  private static final String XINCLUDE_FIXUP_BASE_URI_FEATURE = "http://apache.org/xml/features/xinclude/fixup-base-uris";
  private static final String XINCLUDE_FEATURE = "http://apache.org/xml/features/xinclude";

  private XmlUtils(){

  }

  public static String resolveXincludes(String xml) throws IOException, SAXException, ParserConfigurationException, TransformerException {
    try (StringReader rdr = new StringReader(xml)){
      InputSource source = new InputSource(rdr);
      return toString(toDocument(source));
    }
  }

  private static DocumentBuilderFactory newBuilderFactory() throws ParserConfigurationException {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setXIncludeAware(true);
    dbf.setNamespaceAware(true);
    dbf.setFeature(XINCLUDE_FEATURE, true);
    dbf.setFeature(XINCLUDE_FIXUP_BASE_URI_FEATURE, true);
    return dbf;
  }

  private static Document toDocument(InputSource source) throws ParserConfigurationException, IOException, SAXException {
    return newBuilderFactory().newDocumentBuilder().parse(source);
  }

  private static String toString(Document document) throws IOException, TransformerException {
    try (StringWriter writer = new StringWriter()){
      Transformer t = TransformerFactory.newInstance().newTransformer();
      t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      t.transform(new DOMSource(document), new StreamResult(writer));
      return writer.toString();
    }
  }
}
