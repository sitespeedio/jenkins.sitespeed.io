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
package io.sitespeed.jenkins.graphite;

import io.sitespeed.jenkins.data.Page;
import io.sitespeed.jenkins.data.PageTimings;
import io.sitespeed.jenkins.data.SiteSummary;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConvertToGraphite {

	private static final String[] SUMMARY_METRICS_STOP_LIST = new String[] {
			"values", "standardDev", "variance" };
	private static final Set<String> SUMMARY_METRICS_STOP_SET = new HashSet<String>(
			Arrays.asList(SUMMARY_METRICS_STOP_LIST));

	private final GraphiteConfiguration config;

	public ConvertToGraphite(GraphiteConfiguration config) {
		this.config = config;
	}

	public Map<String, Object> get(Set<Page> pages,
			Set<PageTimings> pageTimings, SiteSummary summary) {

		Map<String, Object> values = new HashMap<String, Object>();

		for (Page page : pages) {

			if (config.isSendRules())
				values.putAll(getRules(page));

			if (config.isSendPageMetrics())
				values.putAll(getMetrics(page));
		}

		if (config.isSendTimings()) {
			for (PageTimings timings : pageTimings) {
				values.putAll(getTimings(timings));

			}

		}

		if (config.isSendSummary()) {
			values.putAll(getSummary(summary));
		}

		return values;
	}

	private Map<String, Object> getSummary(SiteSummary summary) {
		Map<String, Object> values = new HashMap<String, Object>();

		String key = config.getNamespace() + ".summary.";

		Map<String, HashMap<String, String>> theSummary = summary.getValues();
		for (String measurement : theSummary.keySet()) {
			Map<String, String> name = theSummary.get(measurement);

			// don't send that extra info
			for (String math : name.keySet()) {
				if (!SUMMARY_METRICS_STOP_SET.contains(math)) {
					String value = name.get(math);
					values.put(key + measurement + "." + math, value);
				}
			}
		}

		values.put(key + "num_of_pages", summary.getNumberOfPages());
		return values;
	}

	private Map<String, Object> getTimings(PageTimings timings) {

		Map<String, Object> values = new HashMap<String, Object>();
		String browser = timings.getBrowser();
		String key = config.getNamespace() + ".timings." + browser + "."
				+ getGraphiteKey(timings.getUrl());
		Map<String, HashMap<String, String>> measurements = timings
				.getMeasurements();
		for (String measurement : measurements.keySet()) {
			Map<String, String> name = measurements.get(measurement);
			for (String math : name.keySet()) {
				String value = name.get(math);
				values.put(key + measurement + "." + math, value);
			}

		}
		values.put(config.getNamespace() + ".timings." + browser + "."
				+ "runs_per_page", timings.getNumOfRuns());
		return values;

	}

	private Map<String, Object> getRules(Page page) {

		Map<String, Object> values = new HashMap<String, Object>();
		String key = config.getNamespace() + ".rules."
				+ getGraphiteKey(page.getUrl());

		for (Map.Entry<String, Integer> entry : page.getRuleAndScore()
				.entrySet()) {
			values.put(key + entry.getKey(), entry.getValue());
		}

		values.put(key + "total_score", page.getScore());

		return values;
	}

	private Map<String, Object> getMetrics(Page page) {

		// TODO make this cleaner
		Map<String, Object> values = new HashMap<String, Object>();

		String key = config.getNamespace() + ".metrics.assets.num."
				+ getGraphiteKey(page.getUrl());

		for (Map.Entry<String, Integer> entry : page.getAssetsAndNumber()
				.entrySet()) {
			values.put(key + entry.getKey(), entry.getValue());
		}
		
		values.put(key + "hosts", page.getHosts());
		
		key = config.getNamespace() + ".metrics.assets.size."
				+ getGraphiteKey(page.getUrl());
		for (Map.Entry<String, String> entry : page.getAssetsAndSize()
				.entrySet()) {
			values.put(key + entry.getKey(), entry.getValue());
		}

		return values;
	}

	private String getGraphiteKey(String url) {
		// TODO cleanup
		try {
			URL theUrl = new URL(url);
			String protocol = theUrl.getProtocol();
			String path = theUrl.getPath();
			if (path.contains("."))
				path = path.replace(".", "_");
			if (path.contains("~"))
				path = path.replace("~", "_");
			if ("".equals(path) || "/".equals(path))
				return protocol + ".slash.";
			else {
				String graphitePath = protocol + path.replace('/', '.');
				if (graphitePath.endsWith("."))
					return graphitePath;
				else
					return graphitePath + ".";
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}

	}
}
