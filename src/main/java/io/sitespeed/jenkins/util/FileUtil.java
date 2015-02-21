/**
 * Sitespeed.io - How speedy is your site? (http://www.sitespeed.io)
 * Copyright (c) 2014, Peter Hedenskog, Tobias Lidskog
 * and other contributors
 * Released under the Apache 2.0 License
 */
package io.sitespeed.jenkins.util;

import hudson.model.AbstractBuild;
import io.sitespeed.jenkins.SitespeedConstants;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.util.Arrays;
import java.util.Comparator;


public final class FileUtil {

  private static final FileUtil INSTANCE = new FileUtil();

  /**
   * Create a new utils.
   */
  private FileUtil() {}

  /**
   * Get the instance.
   * 
   * @return the singleton instance.
   */
  public static FileUtil getInstance() {
    return INSTANCE;
  }

  public void storeFile(String path, String data, PrintStream logStream) {
    Writer writer = null;

    try {
        writer = new BufferedWriter(new OutputStreamWriter(
              new FileOutputStream(path)));
        writer.write(data);
    } catch (IOException ex) {
      logStream.println("Couldn't store the file " + path + ex);
    } finally {
       try {writer.close();} catch (Exception ex) {
         logStream.println("Couldn't close the writer for " + path + ex);
       }
    }
  }
  
  public File getLastModifiedFileInDir(File dir) {

    if (!dir.isDirectory())
      throw new IllegalArgumentException(dir.getAbsolutePath() + " is not a directory");

    File[] files = dir.listFiles();

    Arrays.sort(files, new Comparator<File>() {
      public int compare(File f1, File f2) {
        return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
      }
    });

    return files[files.length - 1];

  }

  public String getLastModifiedFileNameInDir(File dir) {

    if (!dir.isDirectory())
      throw new IllegalArgumentException(dir.getAbsolutePath() + " is not a directory");

    File[] files = dir.listFiles();

    Arrays.sort(files, new Comparator<File>() {
      public int compare(File f1, File f2) {
        return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
      }
    });

    return files[files.length - 1].getName();

  }

  public File getSitespeedOutputDir(AbstractBuild<?, ?> build) {

    // Get the output result dir, right now we use the workspace
    // TODO make configurable and check for the -r parameter in the input

    return new File(build.getWorkspace().getRemote() + "/" + build.getNumber() + "/"
        + SitespeedConstants.DEFAULT_OUTPUT_DIR);
  }


}
