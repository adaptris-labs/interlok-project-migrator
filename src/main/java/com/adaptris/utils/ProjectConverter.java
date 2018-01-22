package com.adaptris.utils;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author mwarman
 */
public class ProjectConverter {

  private static final String PREFIX = "${";
  private static final String SUFFIX = "}";


  public ProjectConverter(){
  }

  public static void main(String... args) throws IOException, ParserConfigurationException, SAXException, ArchiveException {
    ProjectConverter projectConverter = new ProjectConverter();
    projectConverter.convert(args);
  }

  private void convert(String... args) throws IOException, ParserConfigurationException, SAXException, ArchiveException {
    if (args.length < 2){
      usage();
    }
    String xml = readFile(args[0], StandardCharsets.UTF_8);
    Map<String, String> variables = new HashMap<>();
    boolean first = true;
    for (String arg : args){
      if (first){
        first = false;
        continue;
      }
      variables.putAll(loadProperties(arg));
    }
    File variablesFile = new File("./build/staged/variables.properties");
    writeProperties(variablesFile, variables);
    Map<String, String> variableXPaths = convert(xml, variables);
    File projectJson = new File("./build/staged/config-project.json");
    FileUtils.writeStringToFile(projectJson, createJson("project", variableXPaths), StandardCharsets.UTF_8);
    File zipFile = new File("./build/staged/project.zip");
    ZipUtils.addFilesToZip(zipFile, projectJson, new File(args[0]), variablesFile);
    System.out.println(String.format("Written to [%s]", zipFile.getAbsolutePath()));
  }

  public Map<String, String> convert(String xml, Map<String, String > variables) throws ParserConfigurationException, SAXException, IOException {
    Map<String, String> map = new HashMap<>();
    for (Map.Entry<String, String> entry : variables.entrySet()) {
      List<String> xpaths = XpathUtils.getXPath(xml, String.format(PREFIX + "%s" + SUFFIX, entry.getKey()));
      for (String xpath : xpaths) {
        map.put(xpath, entry.getKey());
      }
    }
    return map;
  }

  private void usage(){
    System.out.println("./interlok-project-conveter <adapter-xml> <variable-substitutions> <variable-substitutions..>");
    System.exit(1);
  }

  private String readFile(String path, Charset encoding) throws IOException
  {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded, encoding);
  }

  private Map<String, String> loadProperties(String path) throws IOException {
    Properties properties = new Properties();
    try (InputStream input = new FileInputStream(path)){
      properties.load(input);
    }
    Map<String, String> results  = new HashMap<>();
    for (Map.Entry<Object, Object> entry : properties.entrySet()) {
      results.put((String) entry.getKey(), (String) entry.getValue());
    }
    return results;
  }

  private void writeProperties(File destination, Map<String, String> variables) throws IOException {
    Properties prop = new Properties();
    prop.putAll(variables);
    try (OutputStream output = new FileOutputStream(destination)) {
      prop.store(output, null);
    }
  }

  @SuppressWarnings("unchecked")
  String createJson(String project, Map<String, String> variableXPaths) {
    JSONObject json = new JSONObject();
    json.put("name", project);
    json.put("xincludeXpaths", new JSONObject());
    JSONObject variableSets = new JSONObject();
    variableSets.put("default", new JSONObject());
    json.put("variableSets", variableSets);
    JSONObject vx = new JSONObject();
    for (Map.Entry<String, String> entry : variableXPaths.entrySet()){
      vx.put(entry.getKey(), entry.getValue());
    }
    json.put("variableXpaths", vx);
    return json.toJSONString();
  }

}
