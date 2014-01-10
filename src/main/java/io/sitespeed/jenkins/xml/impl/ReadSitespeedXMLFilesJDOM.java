/**
 * The MIT License
 * 
 * Copyright (c) 2013, Sitespeed.io organization, Peter Hedenskog
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sitespeed.jenkins.xml.impl;

import io.sitespeed.jenkins.SitespeedConstants;
import io.sitespeed.jenkins.data.Page;
import io.sitespeed.jenkins.data.PageTimings;
import io.sitespeed.jenkins.data.SiteSummary;
import io.sitespeed.jenkins.xml.ReadSitespeedXMLFiles;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ReadSitespeedXMLFilesJDOM implements ReadSitespeedXMLFiles {

  /*
   * (non-Javadoc)
   * 
   * @see io.sitespeed.jenkins.xml.GetFiles#getAnalyzedPages(java.io.File, java.io.PrintStream)
   */
  public Set<Page> getAnalyzedPages(File pagesDir, PrintStream logStream) {
    Set<Page> pages = new HashSet<Page>();

    File[] files = pagesDir.listFiles();
    for (File file : files) {
      try {
        pages.add(new XMLToPageJDOM().get(file));

      } catch (IOException e) {
        logStream.print(e);
      }
    }
    return pages;
  }

  /*
   * (non-Javadoc)
   * 
   * @see io.sitespeed.jenkins.xml.GetFiles#getPageTimings(java.io.File, java.io.PrintStream)
   */
  public Set<PageTimings> getPageTimings(File metricsDir, PrintStream logStream) {

    Set<PageTimings> pageTimings = new HashSet<PageTimings>();

    File[] files = metricsDir.listFiles();
    for (File metricsFile : files) {
      try {
        pageTimings.add(new XMLToPageTimingsJDOM().get(metricsFile));

      } catch (IOException e) {
        logStream.print(e);
      }
    }
    return pageTimings;
  }

  /*
   * (non-Javadoc)
   * 
   * @see io.sitespeed.jenkins.xml.GetFiles#getSummary(java.io.File, java.io.PrintStream)
   */
  public SiteSummary getSummary(File summaryDir, PrintStream logStream) {
    try {
      return new XMLToSummaryJDOM().get(new File(summaryDir.getAbsoluteFile() + "/"
          + SitespeedConstants.SUMMARY_XML_FILENAME));
    } catch (IOException e) {
      logStream.println(e);
      return new SiteSummary(Collections.EMPTY_MAP, 0);
    }

  }
}
