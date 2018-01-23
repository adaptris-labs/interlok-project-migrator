package com.adaptris.utils;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author mwarman
 */
public class XpathUtils {

  private static final String UNIQUE_ID_KEY = "unique-id";

  private static final String[] UNIQUE_ID_TO_SKIP = {"adapter", "service-collection", "shared-components"};

  private XpathUtils(){
  }

  public static List<String> getXPath(String file, String textContents) throws IOException, SAXException, ParserConfigurationException {
    int count = StringUtils.countMatches(file, textContents);
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    Document document = docBuilder.parse(new ByteArrayInputStream((file).getBytes()));
    return getXPath(document, textContents, count);
  }

  private static List<String> getXPath(Document document, String textContents, int expectedCount) {
    List<Node> nodes = new ArrayList<>();
    while(expectedCount != nodes.size()){
      nodes.add(getXPath(document.getDocumentElement(), textContents, nodes));
    }
    List<String> xpaths = new ArrayList<>();
    for (Node node : nodes){
      xpaths.add(generateXPath(node));
    }
    return xpaths;
  }

  private static Node getXPath(Node root, String textContents, List<Node> foundNodes) {
    for (int i = 0; i < root.getChildNodes().getLength(); i++) {
      Node node = root.getChildNodes().item(i);
      if (node instanceof Text){
        if (((Text) node).getWholeText().equals(textContents)){
          if(!foundNodes.contains(node.getParentNode())) {
            return node.getParentNode();
          } else {
            return null;
          }
        }
      } else if (node instanceof Element) {
        if (node.getChildNodes().getLength() > 0) {
          Node result = getXPath(node, textContents, foundNodes);
          if (result != null) {
            return result;
          }
        }
      }
    }
    return null;
  }

  private static String generateXPath(Node node){
    if(node == null){
      return null;
    }
    Node parent = node.getParentNode();
    if (parent == null)
    {
      return "";
    }
    String uniqueId = null;
    for (int i = 0; i < node.getChildNodes().getLength(); i++) {
      Node child = node.getChildNodes().item(i);
      if(child instanceof Element){
        if (child.getNodeName().equals(UNIQUE_ID_KEY)){
          if (!Arrays.asList(UNIQUE_ID_TO_SKIP).contains(node.getNodeName())) {
            uniqueId = child.getTextContent();
          }
        }
      }
    }
    int pos  = 0;
    if(node.getParentNode().getChildNodes().getLength() > 1 && uniqueId == null){
      for (int i = 0; i < node.getParentNode().getChildNodes().getLength(); i++) {
        Node child = node.getParentNode().getChildNodes().item(i);
        if(child instanceof Element){
          if (child.getNodeName().equals(node.getNodeName())){
            pos++;
          }
        }
      }
    }
    return generateXPath(parent) + "/" + node.getNodeName() +
        (uniqueId != null ? String.format("[" + UNIQUE_ID_KEY + "=\"%s\"]", uniqueId): "") +
        (pos > 1 ? String.format("[%s]", pos) : "");
  }

}
