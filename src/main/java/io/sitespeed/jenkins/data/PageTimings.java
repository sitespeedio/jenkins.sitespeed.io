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
package io.sitespeed.jenkins.data;

import java.util.HashMap;
import java.util.Map;

public class PageTimings {

	private final String url;
	private final String browser;
	private final String browserVersion;
	private final int numOfRuns;
	private final Map<String, HashMap<String, String>> measurements;
	
	public PageTimings(String url, String browser, String browserVersion, int numOfRuns,
			Map<String, HashMap<String, String>> measurements) {
		super();
		this.url = url;
		this.browser = browser;
		this.browserVersion = browserVersion;
		this.measurements = measurements;
		this.numOfRuns = numOfRuns;
	}

	public String getUrl() {
		return url;
	}

	public String getBrowser() {
		return browser;
	}

	public String getBrowserVersion() {
		return browserVersion;
	}
	
	public int getNumOfRuns() {
		return numOfRuns;
	}

	public Map<String, HashMap<String, String>> getMeasurements() {
		return measurements;
	}

}
