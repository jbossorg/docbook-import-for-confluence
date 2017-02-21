/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.confluence.plugin.docbook_tools.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

/**
 * Utility methods for files related operations.
 * 
 * @author Vlastimil Elias (velias at redhat dot com) (C) 2011 Red Hat Inc.
 */
public class FileUtils {

  private static final Logger log = Logger.getLogger(FileUtils.class);

  public static final String CHARSET_UTF_8 = "UTF-8";

  private static final int BUFFER_SIZE = 2048;

  private static long workdircounter = System.currentTimeMillis();

  /**
   * {@link FileFilter} implementation to return files only.
   */
  public static final FileFilter FILTER_FILE = new FileFilter() {

    @Override
    public boolean accept(File pathname) {
      return pathname.isFile();
    }

  };

  /**
   * {@link FileFilter} implementation to return XML files only (<code>.xml</code> filename suffix).
   */
  public static final FileFilter FILTER_FILE_XML = new FileFilter() {

    @Override
    public boolean accept(File pathname) {
      return pathname.isFile() && pathname.getName().toLowerCase().endsWith(".xml");
    }

  };

  /**
   * {@link FileFilter} implementation to return directories only.
   */
  public static final FileFilter FILTER_DIRECTORY = new FileFilter() {

    @Override
    public boolean accept(File pathname) {
      return pathname.isDirectory();
    }

  };

  /**
   * Prepare working directory to store processed structure into.<br>
   * Directory is created inside system temp directory (read from <code>java.io.tmpdir</code> system property) in
   * <code>docbook_tools</code> subdirectory.
   * 
   * @param optional prefix for directory.
   * @return working directory to be used
   */
  public static synchronized File prepareWorkingDirectory(String prefix) {
    String tmp_dir = System.getProperty("java.io.tmpdir");
    log.debug("Used temp directory: " + tmp_dir);

    File working_dir = new File(tmp_dir, "docbook_tools/" + (prefix != null ? prefix : "") + System.currentTimeMillis()
        + "-" + workdircounter++);
    working_dir.mkdirs();

    log.debug("Used working directory: " + working_dir);
    return working_dir;
  }

  /**
   * Prepare directory in temp area. <br>
   * Directory is created inside system temp directory (read from <code>java.io.tmpdir</code> system property) in
   * <code>docbook_tools</code> subdirectory.
   * 
   * @param dirName name of directory to create
   * @return directory in temp area with given name
   */
  public static File prepareDirectoryInTempArea(String dirName) {
    String tmp_dir = System.getProperty("java.io.tmpdir");
    log.debug("Used temp directory: " + tmp_dir);

    File working_dir = new File(tmp_dir, "docbook_tools/" + dirName);
    working_dir.mkdirs();

    return working_dir;

  }

  /**
   * Read file from input stream into String using {@link #CHARSET_UTF_8} encoding.
   * 
   * @param is stream to read file from. Closed inside this method.
   * @return String from file data
   */
  public static String readFileAsString(InputStream is) throws java.io.IOException {
    BufferedReader reader = null;
    try {
      StringBuilder buffer = new StringBuilder(1000);
      reader = new BufferedReader(new InputStreamReader(is, CHARSET_UTF_8));
      char[] buf = new char[BUFFER_SIZE];
      int numRead = 0;
      while ((numRead = reader.read(buf)) != -1) {
        buffer.append(buf, 0, numRead);
      }
      return buffer.toString();
    } finally {
      if (reader != null)
        reader.close();
    }
  }

  /**
   * Write String content into file in {@value #CHARSET_UTF_8} encoding. If file exists content is replaced with new
   * content.
   * 
   * @param file to write content to
   * @param content to write
   * @throws IOException if we are not able to create or write file.
   */
  public static void writeStringContentToFile(File file, String content) throws IOException {
    if (content == null) {
      content = "";
    }
    OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
    try {
      os.write(content.getBytes(CHARSET_UTF_8));
      os.flush();
    } finally {
      if (os != null) {
        os.close();
      }
    }
  }

  /**
   * Copy UTF-8 file from given input stream into given output stream and replace defined keys with defined values
   * inside. One key may have more occurrences in template, all are replaced. Keys are regexps !, see {@link Pattern}.
   * 
   * @param is stream to read template file from - UTF-8 charset!
   * @param replacements keys and values to replace in copied file.
   * @param os to write file into - UTF-8 charset!
   * @throws Exception
   */
  public static void copyFileWithReplace(InputStream is, Properties replacements, OutputStream os) throws Exception {
    String template = FileUtils.readFileAsString(is);
    for (Object key : replacements.keySet()) {
      template = template.replaceAll(key.toString(), replacements.get(key).toString());
    }
    os.write(template.getBytes(FileUtils.CHARSET_UTF_8));
  }

  /**
   * Copy file from input stream into output stream.
   * 
   * @param is stream to read file from, closed in this method
   * @param os stream to write file into, closed inside of this method.
   */
  public static void copyFile(InputStream is, OutputStream os) throws java.io.IOException {
    try {
      int count;
      byte buf[] = new byte[BUFFER_SIZE];
      while ((count = is.read(buf, 0, BUFFER_SIZE)) != -1) {
        os.write(buf, 0, count);
      }
    } finally {
      closeInputStream(is);
      closeOutputStream(os);
    }
  }

  /**
   * Unzip file with folder structure into given folder.
   * 
   * @param is stream with zip file (closed inside of this method)
   * @param outfolder folder to unzip file into (whole subfolders structure is created here)
   * @throws Exception
   */
  public static void unzip(InputStream is, File outfolder) throws Exception {
    ZipInputStream zis = null;
    BufferedOutputStream dest = null;
    try {

      zis = new ZipInputStream(new BufferedInputStream(is));
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        // write the files to the disk
        File f = new File(outfolder, entry.getName());
        if (entry.isDirectory()) {
          f.mkdirs();
        } else {
          // create folders for file if not created before
          f.getAbsoluteFile().getParentFile().mkdirs();

          dest = new BufferedOutputStream(new FileOutputStream(f), BUFFER_SIZE);

          int count;
          byte buf[] = new byte[BUFFER_SIZE];
          while ((count = zis.read(buf, 0, BUFFER_SIZE)) != -1) {
            dest.write(buf, 0, count);
          }
          closeOutputStream(dest);
          dest = null;
        }
      }
    } finally {
      closeInputStream(zis);
      closeOutputStream(dest);
    }
  }

  /**
   * Zip content of given dir into given stream.
   * 
   * @param zipDir directory to zip
   * @param os stream to write zipped content into
   * @throws Exception
   */
  public static void zip(File zipDir, OutputStream os) throws Exception {
    ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(os));
    try {
      zipDir(zipDir, zos, zipDir.getPath());
    } finally {
      closeOutputStream(zos);
    }
  }

  private static void zipDir(File zipDir, ZipOutputStream zos, String basePath) throws Exception {
    FileInputStream fis = null;
    try {
      File[] dirList = zipDir.listFiles();
      byte[] readBuffer = new byte[BUFFER_SIZE];
      int bytesIn = 0;
      for (int i = 0; i < dirList.length; i++) {
        File f = dirList[i];
        if (f.isDirectory()) {
          zipDir(f, zos, basePath);
        } else {
          fis = new FileInputStream(f);
          ZipEntry anEntry = new ZipEntry(f.getPath().replaceFirst(basePath, ""));
          zos.putNextEntry(anEntry);
          while ((bytesIn = fis.read(readBuffer)) != -1) {
            zos.write(readBuffer, 0, bytesIn);
          }
          fis.close();
          fis = null;
        }
      }
    } finally {
      closeInputStream(fis);
    }
  }

  /**
   * Deep copy of directory structure.
   * 
   * @param sourceLocation source directory to copy all files and directories from
   * @param targetLocation target directory to copy source structure into
   * @throws IOException
   */
  public static void copyDirectory(File sourceLocation, File targetLocation) throws IOException {

    if (sourceLocation.isDirectory()) {
      if (!targetLocation.exists()) {
        targetLocation.mkdirs();
      }

      String[] children = sourceLocation.list();
      for (int i = 0; i < children.length; i++) {
        copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
      }
    } else {

      // create folders for file if not exists yet
      if (!targetLocation.getAbsoluteFile().getParentFile().exists()) {
        targetLocation.getAbsoluteFile().getParentFile().mkdirs();
      }

      InputStream in = null;
      OutputStream out = null;
      try {
        in = openFileInputStream(sourceLocation);
        out = new BufferedOutputStream(new FileOutputStream(targetLocation), BUFFER_SIZE);
        byte[] buf = new byte[BUFFER_SIZE];
        int count;
        while ((count = in.read(buf)) > 0) {
          out.write(buf, 0, count);
        }
      } finally {
        closeInputStream(in);
        closeOutputStream(out);
      }
    }
  }

  /**
   * Open buffered file input stream for given file.
   * 
   * @param sourceLocation to open stream for
   * @return buffered file input stream
   * @throws FileNotFoundException
   */
  public static BufferedInputStream openFileInputStream(File sourceLocation) throws FileNotFoundException {
    return new BufferedInputStream(new FileInputStream(sourceLocation));
  }

  /**
   * Delete file or empty and nonempty directory.
   * 
   * @param toDelete file or directory to delete.
   */
  public static void deleteDirectoryRecursively(File toDelete) {
    if (toDelete != null) {
      if (toDelete.isDirectory()) {
        File[] sub = toDelete.listFiles();
        if (sub != null && sub.length > 0) {
          for (File f : sub) {
            deleteDirectoryRecursively(f);
          }
        }
      }
      toDelete.delete();
    }
  }

  /**
   * Safely close input stream.
   * 
   * @param is
   */
  public static void closeInputStream(InputStream is) {
    if (is != null) {
      try {
        is.close();
      } catch (IOException e) {
      }
    }
  }

  /**
   * Safely close output stream. Flush it before close.
   * 
   * @param os
   */
  public static void closeOutputStream(OutputStream os) {
    if (os != null) {
      try {
        os.flush();
        os.close();
      } catch (IOException e) {
        log.error(e.getMessage(), e);
      }
    }
  }

}
