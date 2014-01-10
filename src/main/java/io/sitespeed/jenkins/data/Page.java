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
package io.sitespeed.jenkins.data;

import java.util.Map;

public class Page {

  private final Map<String, Integer> ruleAndScore;
  private final Integer score;
  private final Integer hosts;
  private final Map<String, String> assetsAndSize;
  private final Map<String, Integer> assetsAndNumber;
  private final String url;

  public Page(String url, Integer score, Map<String, Integer> ruleAndScore, Integer hosts,
      Map<String, String> assetsAndSize, Map<String, Integer> assetsAndNumber) {

    this.url = url;
    this.score = score;
    this.ruleAndScore = ruleAndScore;
    this.hosts = hosts;
    this.assetsAndSize = assetsAndSize;
    this.assetsAndNumber = assetsAndNumber;
  }

  public Map<String, Integer> getRuleAndScore() {
    return ruleAndScore;
  }

  public Integer getScore() {
    return score;
  }

  public Integer getHosts() {
    return hosts;
  }

  public Map<String, String> getAssetsAndSize() {
    return assetsAndSize;
  }

  public Map<String, Integer> getAssetsAndNumber() {
    return assetsAndNumber;
  }

  public String getUrl() {
    return url;
  }

}
