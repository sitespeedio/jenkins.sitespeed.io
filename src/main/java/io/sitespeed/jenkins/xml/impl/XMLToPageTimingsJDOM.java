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

import io.sitespeed.jenkins.data.PageTimings;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaderSAX2Factory;

public class XMLToPageTimingsJDOM {

  public XMLToPageTimingsJDOM() {}

  /*
   * (non-Javadoc)
   * 
   * @see io.sitespeed.jenkins.xml.XMLToPageTimingsConverter#get(java.io.File)
   */
  public PageTimings get(File browserTimeXML) throws IOException {

    final SAXBuilder b = new SAXBuilder(new XMLReaderSAX2Factory(false));
    Document doc;
    try {
      doc = b.build(browserTimeXML);
    } catch (JDOMException e) {
      throw new IOException(e);
    }

    return new PageTimings(getPageData("actualUrl", doc), getPageData("browserName", doc),
        getPageData("browserVersion", doc), doc.getRootElement().getChild("runs")
            .getChildren("run").size(), getMeasurements(doc));
  }

  private String getPageData(String name, Document doc) {

    for (Element el : doc.getRootElement().getChild("pageData").getChildren("entry")) {
      if (el.getChildText("key") != null && el.getChild("key").getValue().equals(name)) {
        return el.getChildText("value");
      }
    }
    return "";
  }

  private Map<String, HashMap<String, String>> getMeasurements(Document doc) {

    List<Element> stats = doc.getRootElement().getChild("statistics").getChildren("statistic");
    Map<String, HashMap<String, String>> data = new HashMap<String, HashMap<String, String>>();

    for (Element statistic : stats) {
      String name = statistic.getChild("name").getValue();
      HashMap<String, String> values = new HashMap<String, String>();

      for (Element element : statistic.getChildren()) {
        if (!element.getName().equals("name")) {
          values.put(element.getName(), element.getValue());
        }
      }

      data.put(name, values);

    }

    return data;
  }
}
