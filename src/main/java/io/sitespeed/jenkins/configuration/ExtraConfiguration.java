/**
 * Sitespeed.io - How speedy is your site? (http://www.sitespeed.io) Copyright (c) 2014, Peter
 * Hedenskog, Tobias Lidskog and other contributors Released under the Apache 2.0 License
 */
package io.sitespeed.jenkins.configuration;

import org.kohsuke.stapler.DataBoundConstructor;

public class ExtraConfiguration {

  private final String sitespeedConfiguration;


  @DataBoundConstructor
  public ExtraConfiguration(String sitespeedConfiguration) {
    this.sitespeedConfiguration = sitespeedConfiguration;

  }

  public String getSitespeedConfiguration() {
    return sitespeedConfiguration;
  }

}
