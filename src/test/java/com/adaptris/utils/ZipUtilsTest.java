package com.adaptris.utils;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author mwarman
 */
public class ZipUtilsTest {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private File folder;

  @Before
  public void before() throws IOException {
    folder = temporaryFolder.newFolder();
  }

  @Test
  public void addFilesToZip() throws Exception {
    File adpXml = new File(this.getClass().getClassLoader().getResource("adapter.xml").getFile());
    File vars = new File(this.getClass().getClassLoader().getResource("variables.properties").getFile());
    File zip = new File(folder, "junit.zip");
    ZipUtils.addFilesToZip(zip, adpXml, vars);
    assertZipContent(zip);
  }

  private void assertZipContent(File destination) throws IOException {
    ZipFile zipFile = new ZipFile(destination);

    ZipArchiveEntry adp = zipFile.getEntry("adapter.xml");
    assertNotNull(adp);

    ZipArchiveEntry vars = zipFile.getEntry("variables.properties");
    assertNotNull(vars);

    Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
    int numberOfEntries = 0;
    while (entries.hasMoreElements()) {
      numberOfEntries++;
      entries.nextElement();
    }
    assertEquals(2, numberOfEntries);
  }

}