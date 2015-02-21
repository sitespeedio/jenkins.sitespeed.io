/**
 * Sitespeed.io - How speedy is your site? (http://www.sitespeed.io)
 * Copyright (c) 2014, Peter Hedenskog, Tobias Lidskog
 * and other contributors
 * Released under the Apache 2.0 License
 */

package io.sitespeed.jenkins;

import hudson.model.Action;

public class SitespeedLinkAction implements Action {
  private final String description;
  private final String name;
  private final String icon;
  private final String url;

  public SitespeedLinkAction(String name, String url, String icon, String description) {
    this.description = description;
    this.name = name;
    this.icon = icon;
    this.url = url;
  }

  public String getDisplayName() {
    return name;
  }

  public String getIconFileName() {
    return icon;
  }

  public String getUrlName() {
    return url;
  }

  public String getDescription() {
    return description;
  }
}
