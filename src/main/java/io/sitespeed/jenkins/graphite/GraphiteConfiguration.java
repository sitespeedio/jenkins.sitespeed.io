/**
 * Sitespeed.io - How speedy is your site? (http://www.sitespeed.io)
 * Copyright (c) 2014, Peter Hedenskog, Tobias Lidskog
 * and other contributors
 * Released under the Apache 2.0 License
 */
package io.sitespeed.jenkins.graphite;

import org.kohsuke.stapler.DataBoundConstructor;

public class GraphiteConfiguration {

    private final String host;
    private final int port;
    private final boolean sendRules;
    private final boolean sendPageMetrics;
    private final boolean sendTimings;
    private final boolean sendSummary;
    private final String namespace;

  @DataBoundConstructor
      public GraphiteConfiguration(String host, int port, String namespace, boolean sendRules,
				   boolean sendPageMetrics, boolean sendTimings, boolean sendSummary) {
      this.host = host;
      this.port = port;
      if (namespace != null && namespace.endsWith("."))
	  this.namespace = namespace.substring(0, namespace.length() - 1);
      else
	  this.namespace = namespace;
      this.sendRules = sendRules;
      this.sendPageMetrics = sendPageMetrics;
      this.sendTimings = sendTimings;
      this.sendSummary = sendSummary;
  }

    public String getHost() {
	return host;
    }

    public int getPort() {
	return port;
    }

    public String getNamespace() {
	return namespace;
    }

    public boolean isSendRules() {
	return sendRules;
    }

    public boolean isSendPageMetrics() {
	return sendPageMetrics;
    }

    public boolean isSendTimings() {
	return sendTimings;
    }

    public boolean isSendSummary() {
	return sendSummary;
    }

}