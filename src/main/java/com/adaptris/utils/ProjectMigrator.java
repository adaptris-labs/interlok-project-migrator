package com.adaptris.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author mwarman
 */
public class ProjectMigrator {

  private final Options options;

  private String project;
  private String adapterPath;
  private String[] variablesPaths;
  private String variableFileName;
  private String[] variableSets;
  private String testConfigFileName;

  private static final String PREFIX = "${";
  private static final String SUFFIX = "}";
  
  private static final String OUTPUT_DIRECTORY = "./build/staged/";
  private static final String BUILD_DIRECTORY  = OUTPUT_DIRECTORY + "build/";
  private static final String CONFIG_FILE      = "config-project.json";

  private static final String HELP_ARG = "help";
  private static final String PROJECT_ARG = "project";
  private static final String ADAPTER_ARG = "adapter";
  private static final String VARIABLES_ARG = "variables";
  private static final String TEST_CONFIG_FILE_NAME_ARG = "testConfigFileName";
  private static final String VARIABLE_FILE_NAME_ARG = "variableFileName";
  private static final String VARIABLE_SETS = "variableSets";

  private static final String DEFAULT_PROJECT = "project";

  ProjectMigrator(){
    options = new Options();
    options.addOption("h",HELP_ARG, false, "Displays this.." );
    options.addOption("p", PROJECT_ARG, true, "The project name");
    options.addRequiredOption("a", ADAPTER_ARG, true, "(required) The adapter xml");
    Option variables = new Option("v", VARIABLES_ARG, true, "(required) The variables (can be added multiple times)");
    variables.setArgs(Option.UNLIMITED_VALUES);
    variables.setRequired(true);
    options.addOption(variables);
    Option variableSets = new Option("s", VARIABLE_SETS, true, "The variables sets");
    variableSets.setArgs(Option.UNLIMITED_VALUES);
    options.addOption(variableSets);
    options.addOption("f", VARIABLE_FILE_NAME_ARG, true, "The variable file name");
    options.addOption("t", TEST_CONFIG_FILE_NAME_ARG, true, "The test config file name");

  }

  public static void main(String[] args) throws Exception {
    ProjectMigrator projectMigrator = new ProjectMigrator();
    projectMigrator.convert(args);
  }

  void convert(String[] args) throws Exception {
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
      variableFileName = line.getOptionValue(VARIABLE_FILE_NAME_ARG);
      variableSets = line.getOptionValues(VARIABLE_SETS);
      testConfigFileName = line.getOptionValue(TEST_CONFIG_FILE_NAME_ARG);
    } catch (ParseException e) {
      System.err.println("Parsing failed.  Reason: " + e.getMessage());
      usage();
    }
  }

  void convert(String project, String adapterPath, String... variablesPaths) throws Exception {
    String xml = XmlUtils.resolveXincludes(readFile(adapterPath));
    Map<String, String> variables = new HashMap<>();
    for (String arg : variablesPaths){
      variables.putAll(loadProperties(arg));
    }
    File buildDirectory = new File(BUILD_DIRECTORY);
    buildDirectory.mkdir();
    Map<String, String> variableXPaths = convert(xml, variables);
    File projectJson = new File(buildDirectory, CONFIG_FILE);
    FileUtils.writeStringToFile(projectJson, createJson(project, variableXPaths), StandardCharsets.UTF_8);
    System.out.println(String.format("Written to [%s]", buildDirectory));
  }

  Map<String, String> convert(String xml, Map<String, String> variables) throws Exception {
    Map<String, String> map = new TreeMap<>();
    for (Map.Entry<String, String> entry : variables.entrySet()) {
      String variable = String.format(PREFIX + "%s" + SUFFIX, entry.getKey());
      int expectedCount = StringUtils.countMatches(xml, variable);
      Document document = XpathUtils.createDocument(xml);
      List<String> xpaths = XpathUtils.getXPath(document, variable, expectedCount);
      for (String xpath : xpaths) {
        map.put(xpath, XpathUtils.evaluateXpath(document, xpath));
      }
    }
    return map;
  }

  void usage(){
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp( "interlok-project-migrator", options );
    System.exit(1);
  }

  String readFile(String path) throws IOException {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded, StandardCharsets.UTF_8);
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

  private String createJson(String project, Map<String, String> variableXPaths) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode json = mapper.createObjectNode();
    json.put("name", project);
    if (StringUtils.isNotEmpty(testConfigFileName)){
      json.put("testConfigFileName", testConfigFileName);
    }
    if (StringUtils.isNotEmpty(variableFileName)){
      json.put("variableFileName", variableFileName);
    }
    ObjectNode vs = mapper.createObjectNode();
    json.set("variableSets", vs);
    if (variableSets != null && variableSets.length > 0) {
      for(String variableSet : variableSets){
        vs.set(variableSet, mapper.createObjectNode());
      }
    } else {
      vs.set("default", mapper.createObjectNode());
    }
    ObjectNode variableXpaths = mapper.createObjectNode();
    json.set("variableXpaths", variableXpaths);
    for (Map.Entry<String, String> entry : variableXPaths.entrySet()){
      variableXpaths.put(entry.getKey(), entry.getValue());
    }
    json.put("uidInXincludeCompntListFileName", false);
    json.set("xincludeXpaths", mapper.createObjectNode());
    json.put("structured", true);
    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
  }

}
