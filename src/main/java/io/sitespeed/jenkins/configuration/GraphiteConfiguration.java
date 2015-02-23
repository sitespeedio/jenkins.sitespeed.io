/**
 * Sitespeed.io - How speedy is your site? (http://www.sitespeed.io) Copyright (c) 2014, Peter
 * Hedenskog, Tobias Lidskog and other contributors Released under the Apache 2.0 License
 */
package io.sitespeed.jenkins.configuration;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Configure Graphite.
 * 
 * @author peter
 * 
 */
public class GraphiteConfiguration {

  private final String host;
  private final int port;

  private final String namespace;

  @DataBoundConstructor
  public GraphiteConfiguration(String host, int port, String namespace) {
    this.host = host;
    this.port = port;
    if (namespace != null && namespace.endsWith("."))
      this.namespace = namespace.substring(0, namespace.length() - 1);
    else
      this.namespace = namespace;

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


}
