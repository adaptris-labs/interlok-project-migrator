package com.adaptris.utils;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;

/**
 * @author mwarman
 */
public class ZipUtils {

  private ZipUtils(){

  }

  public static void addFilesToZip(File destination, File... source) throws IOException, ArchiveException {
    OutputStream archiveStream = new FileOutputStream(destination);
    ArchiveOutputStream archive = new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.ZIP, archiveStream);
    for (File file : source) {
      String entryName = file.getName();
      ZipArchiveEntry entry = new ZipArchiveEntry(entryName);
      archive.putArchiveEntry(entry);

      BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));

      IOUtils.copy(input, archive);
      input.close();
      archive.closeArchiveEntry();
    }
    archive.finish();
    archiveStream.close();
  }

}
