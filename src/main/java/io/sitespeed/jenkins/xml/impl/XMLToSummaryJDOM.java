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

import io.sitespeed.jenkins.data.SiteSummary;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaderSAX2Factory;

public class XMLToSummaryJDOM {

  public SiteSummary get(File summaryXML) throws IOException {

    final SAXBuilder b = new SAXBuilder(new XMLReaderSAX2Factory(false));
    Document doc;
    try {
      doc = b.build(summaryXML);
    } catch (JDOMException e) {
      throw new IOException(e);
    }

    Map<String, HashMap<String, String>> values = new HashMap<String, HashMap<String, String>>();
    // TODO today the cache time is in seconds, probably should be converted to minutes?
    for (Element metric : doc.getRootElement().getChild("metrics").getChildren()) {
      String name = metric.getName();
      HashMap<String, String> the = new HashMap<String, String>();
      for (Element valueType : metric.getChildren()) {
        the.put(valueType.getName(), valueType.getValue());
      }
      values.put(name, the);
    }

    int pages = new Integer(doc.getRootElement().getChild("pages").getValue());

    return new SiteSummary(values, pages);
  }



}
