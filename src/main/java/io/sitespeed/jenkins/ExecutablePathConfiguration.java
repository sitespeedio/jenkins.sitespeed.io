/**
 * Sitespeed.io - How speedy is your site? (http://www.sitespeed.io) Copyright (c) 2014, Peter
 * Hedenskog, Tobias Lidskog and other contributors Released under the Apache 2.0 License
 */
package io.sitespeed.jenkins;

import org.kohsuke.stapler.DataBoundConstructor;

public class ExecutablePathConfiguration {

  private final String sitespeedExecutable;

  @DataBoundConstructor
  public ExecutablePathConfiguration(String sitespeedExecutable) {
    this.sitespeedExecutable = sitespeedExecutable;

  }

  public String getSitespeedExecutable() {
    return sitespeedExecutable;
  }

}
