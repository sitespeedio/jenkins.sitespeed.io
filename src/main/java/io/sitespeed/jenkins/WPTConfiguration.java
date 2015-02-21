/**
 * Sitespeed.io - How speedy is your site? (http://www.sitespeed.io) Copyright (c) 2014, Peter
 * Hedenskog, Tobias Lidskog and other contributors Released under the Apache 2.0 License
 */
package io.sitespeed.jenkins;

import org.kohsuke.stapler.DataBoundConstructor;

public class WPTConfiguration {

  private final String wptHost; 
  private final String wptKey;
  private final String wptConfig;


  @DataBoundConstructor
  public WPTConfiguration(String wptHost, String wptKey, String wptConfig ) {
    this.wptHost = wptHost;
    this.wptKey = wptKey;
    this.wptConfig = wptConfig;

  }

  public String getWptHost() {
    return wptHost;
  }


  public String getWptKey() {
    return wptKey;
  }


  public String getWptConfig() {
    return wptConfig;
  }

}
