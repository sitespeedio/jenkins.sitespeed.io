/**
 * The MIT License
 *
 * Copyright (c) 2013, Sitespeed.io organization, Peter Hedenskog 
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.sitespeed.jenkins.xml.impl;

import io.sitespeed.jenkins.data.Page;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaderSAX2Factory;

/**
 * Convert a sitespeed.io XML to an Page object.
 * 
 * @author peter
 * 
 */
public class XMLToPageJDOM {

	public XMLToPageJDOM() {
	}

	public Page get(File pageXML) throws IOException {

		final SAXBuilder b = new SAXBuilder(new XMLReaderSAX2Factory(false));
		Document doc;
		try {
			doc = b.build(pageXML);
		} catch (JDOMException e) {
			throw new IOException(e);
		}

		int numOfHosts = doc.getRootElement().getChild("g").getChild("ydns")
				.getChild("components").getChildren("item").size();

		String url = doc.getRootElement().getChildText("curl");
		Integer score = new Integer(doc.getRootElement().getChildText("o"));

		return new Page(url, score, getRules(doc), numOfHosts,
				getAssetsSize(doc), getNumberOfAssets(doc));
	}

	private Map<String, Integer> getRules(Document doc) {
		Map<String, Integer> rulesMap = new HashMap<String, Integer>();
		// add all the rules
		Element rules = doc.getRootElement().getChild("g");
		for (Element rule : rules.getChildren()) {
			rulesMap.put(rule.getName(), new Integer(rule.getChild("score")
					.getValue()));
		}
		return rulesMap;
	}

	private Map<String, String> getAssetsSize(Document doc) {
		Map<String, String> assetsAndSize = new HashMap<String, String>();

		Element assets = doc.getRootElement().getChild("stats");
		for (Element asset : assets.getChildren()) {

			// The sizes in YSlow xml is sometimes wrong, need to calculate?!?!
			assetsAndSize.put(asset.getName(), asset.getChild("w")
					.getValue());
		}
		return assetsAndSize;
	}

	private Map<String, Integer> getNumberOfAssets(Document doc) {
		Map<String, Integer> assetsAndNumber = new HashMap<String, Integer>();

		Element assets = doc.getRootElement().getChild("stats");
		for (Element asset : assets.getChildren()) {
			assetsAndNumber.put(asset.getName(), new Integer(asset
					.getChild("r").getValue()));
		}
		return assetsAndNumber;
	}

}
