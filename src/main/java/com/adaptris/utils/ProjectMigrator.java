package com.adaptris.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.cli.*;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
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
public class ProjectMigrator {

  private Options options;

  private String project;
  private String adapterPath;
  private String[] variablesPaths;

  private static final String PREFIX = "${";
  private static final String SUFFIX = "}";
  
  private static final String OUTPUT_DIRECTORY = "./build/staged/";
  private static final String BUILD_DIRECTORY  = OUTPUT_DIRECTORY + "build/";
  private static final String ADAPTER_FILE     = "adapter.xml";
  private static final String VARIABLES_FILE   = "variables.properties";
  private static final String CONFIG_FILE      = "config-project.json";
  private static final String PROJECT_EXT      = ".zip";

  private static final String HELP_ARG = "help";
  private static final String PROJECT_ARG = "project";
  private static final String ADAPTER_ARG = "adapter";
  private static final String VARIABLES_ARG = "variables";

  private static final String DEFAULT_PROJECT = "project";

  ProjectMigrator(){
    options = new Options();
    options.addOption("h",HELP_ARG, false, "Displays this.." );
    options.addOption("p", PROJECT_ARG, true, "The project name");
    options.addRequiredOption("a", ADAPTER_ARG, true, "(required) The adapter xml");
    Option option = new Option("v", VARIABLES_ARG, true, "(required) The variables (can be added multiple times)");
    option.setArgs(Option.UNLIMITED_VALUES);
    option.setRequired(true);
    options.addOption(option);
  }

  public static void main(String[] args) throws Exception {
    ProjectMigrator projectMigrator = new ProjectMigrator();
    projectMigrator.convert(args);
  }

  void convert(String[] args) throws ParserConfigurationException, ArchiveException, SAXException, TransformerException, IOException {
    arguments(args);
    convert(project, adapterPath, variablesPaths);
  }

  void arguments(String[] args){
    CommandLineParser parser = new DefaultParser();
    try {
      CommandLine line = parser.parse(options, args);
      if(line.hasOption(HELP_ARG)){
        usage();
      }
      if (line.hasOption(PROJECT_ARG)) {
        project = line.getOptionValue(PROJECT_ARG);
      } else {
        project = DEFAULT_PROJECT;
      }
      adapterPath = line.getOptionValue(ADAPTER_ARG);
      variablesPaths = line.getOptionValues(VARIABLES_ARG);
    } catch (ParseException e) {
      System.err.println("Parsing failed.  Reason: " + e.getMessage());
      usage();
    }
  }

  void convert(String project, String adapterPath, String... variablesPaths) throws IOException, ParserConfigurationException, SAXException, ArchiveException, TransformerException {
    String xml = XmlUtils.resolveXincludes(readFile(adapterPath, StandardCharsets.UTF_8));
    Map<String, String> variables = new HashMap<>();
    for (String arg : variablesPaths){
      variables.putAll(loadProperties(arg));
    }
    File buildDirectory = new File(BUILD_DIRECTORY);
    buildDirectory.mkdir();
    File adapterFile = new File(buildDirectory, ADAPTER_FILE);
    File variablesFile = new File(buildDirectory, VARIABLES_FILE);
    writeProperties(variablesFile, variables);
    Map<String, String> variableXPaths = convert(xml, variables);
    File projectJson = new File(buildDirectory, CONFIG_FILE);
    FileUtils.writeStringToFile(projectJson, createJson(project, variableXPaths), StandardCharsets.UTF_8);
    FileUtils.writeStringToFile(adapterFile, xml , StandardCharsets.UTF_8);
    System.out.println(String.format("Written to [%s]", buildDirectory));
  }

  Map<String, String> convert(String xml, Map<String, String> variables) throws ParserConfigurationException, SAXException, IOException {
    Map<String, String> map = new HashMap<>();
    for (Map.Entry<String, String> entry : variables.entrySet()) {
      List<String> xpaths = XpathUtils.getXPath(xml, String.format(PREFIX + "%s" + SUFFIX, entry.getKey()));
      for (String xpath : xpaths) {
        map.put(xpath, entry.getKey());
      }
    }
    return map;
  }

  void usage(){
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp( "interlok-project-migrator", options );
    System.exit(1);
  }

  String readFile(String path, Charset encoding) throws IOException {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded, encoding);
  }

  Map<String, String> loadProperties(String path) throws IOException {
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

  void writeProperties(File destination, Map<String, String> variables) throws IOException {
    Properties prop = new Properties();
    prop.putAll(variables);
    try (OutputStream output = new FileOutputStream(destination)) {
      prop.store(output, null);
    }
  }

  @SuppressWarnings("unchecked")
  String createJson(String project, Map<String, String> variableXPaths) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode json = mapper.createObjectNode();
    json.put("name", project);
    ObjectNode variableSets = mapper.createObjectNode();
    json.set("variableSets", variableSets);
    for (Map.Entry<String, String> entry : variableXPaths.entrySet()){
      variableSets.put(entry.getKey(), entry.getValue());
    }
    json.set("variableXpaths", mapper.createObjectNode());
    json.put("uidInXincludeCompntListFileName", false);
    json.set("xincludeXpaths", mapper.createObjectNode());
    json.put("structured", false);
    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
  }

}
