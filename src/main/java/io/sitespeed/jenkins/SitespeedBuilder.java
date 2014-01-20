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
package io.sitespeed.jenkins;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import io.sitespeed.jenkins.data.Page;
import io.sitespeed.jenkins.data.PageTimings;
import io.sitespeed.jenkins.data.SiteSummary;
import io.sitespeed.jenkins.graphite.ConvertToGraphite;
import io.sitespeed.jenkins.graphite.GraphiteConfiguration;
import io.sitespeed.jenkins.graphite.GraphiteSender;
import io.sitespeed.jenkins.util.FileUtil;
import io.sitespeed.jenkins.xml.ReadSitespeedXMLFiles;
import io.sitespeed.jenkins.xml.impl.ReadSitespeedXMLFilesJDOM;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class SitespeedBuilder extends Builder {

  /**
   * The configuration sent to the sitespeed script.
   */
  private final String sitespeedConfiguration;

  /**
   * The JUnit configuration.
   */
  private final String junitConfiguration;

  /**
   * The home dir of the sitespeed script.
   */
  private final String home;

  /**
   * If Graphite is checked or not.
   */
  private final boolean checkGraphite;

  /**
   * Configuration for Graphite.
   */
  private final GraphiteConfiguration graphiteConfig;

  /**
   * The URL to the sitespeed logo.
   */
  private static final String ICON_URL = "/plugin/sitespeed/logo48x48.png";

  @DataBoundConstructor
  public SitespeedBuilder(String sitespeedConfiguration, String junitConfiguration, String home,
      GraphiteConfiguration checkGraphite) {
    this.sitespeedConfiguration = sitespeedConfiguration;

    if (home != null && !home.endsWith(File.separator))
      this.home = home + File.separator;
    else
      this.home = home;
    this.junitConfiguration = junitConfiguration;

    graphiteConfig = checkGraphite;
    this.checkGraphite = checkGraphite == null ? false : true;

  }

  @Override
  public DescriptorImpl getDescriptor() {
    return (DescriptorImpl) super.getDescriptor();
  }

  public String getHome() {
    return home;
  }

  public String getHost() {
    return graphiteConfig != null ? graphiteConfig.getHost() : "";
  }

  public String getJunitConfiguration() {
    return junitConfiguration;
  }

  public String getNamespace() {
    return graphiteConfig != null ? graphiteConfig.getNamespace() : "";
  }

  public String getPort() {
    return graphiteConfig != null ? graphiteConfig.getPort() + "" : "";
  }

  public String getSitespeedConfiguration() {
    return sitespeedConfiguration;
  }

  public boolean isCheckGraphite() {
    return checkGraphite;
  }

  public boolean isSendPageMetrics() {
    return graphiteConfig != null ? graphiteConfig.isSendPageMetrics() : true;
  }

  public boolean isSendRules() {
    return graphiteConfig != null ? graphiteConfig.isSendRules() : true;
  }

  public boolean isSendTimings() {
    return graphiteConfig != null ? graphiteConfig.isSendTimings() : true;
  }

  public boolean isSendSummary() {
    return graphiteConfig != null ? graphiteConfig.isSendSummary() : true;
  }

  @Override
  public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {

    EnvVars env = getEnvironment(build, listener);
    PrintStream logStream = listener.getLogger();

    // TODO check that everything is setup ok.
    if (!validateInput(build, logStream)) return false;

    File sitespeedOutputDir = FileUtil.getInstance().getSitespeedOutputDir(build);

    try {
      // First run sitespeed
      int returnValue = runSitespeed(sitespeedOutputDir.getAbsolutePath(), env, logStream);
      String domainDirName =
          FileUtil.getInstance().getLastModifiedFileNameInDir(sitespeedOutputDir);

      String dateDirName =
          FileUtil.getInstance().getLastModifiedFileNameInDir(
              FileUtil.getInstance().getLastModifiedFileInDir(sitespeedOutputDir));

      String thisRunsOutputDir = sitespeedOutputDir + "/" + domainDirName + "/" + dateDirName;

      if (returnValue == 0) {
        returnValue =
            runJUnit(sitespeedOutputDir.getAbsolutePath(), build.getWorkspace().getRemote(), env,
                logStream);

        if (returnValue == 0 && checkGraphite) sendToGraphite(thisRunsOutputDir, logStream);
      }

      String url =
          createUrlToAnalyzedPages(build.getProject().getName(), build.getNumber(), domainDirName,
              dateDirName, "index.html");

      build.addAction(new SitespeedLinkAction("Sitespeed.io report", url, ICON_URL,
          "This is a sitespeed.io report"));

      return returnValue == 0 ? true : false;

    } catch (InterruptedException e) {
      logStream.println(e);
    }

    return false;
  }

  private String createUrlToAnalyzedPages(String projectName, int buildNumber,
      String domainDirName, String dateDirName, String htmlFile) {
    StringBuilder b = new StringBuilder("/job/");
    b.append(projectName).append("/ws/").append(buildNumber).append("/")
        .append(SitespeedConstants.DEFAULT_OUTPUT_DIR).append("/").append(domainDirName)
        .append("/").append(dateDirName).append("/").append(htmlFile);
    return b.toString();

  }

  private int runJUnit(String resultBaseDir, String outputDir, EnvVars env, PrintStream logStream)
      throws InterruptedException {
    BashRunner runner = new BashRunner(home);
    ParameterHelper paramHelper = new ParameterHelper(logStream);
    List<String> args = paramHelper.getJUnitParamters(junitConfiguration, outputDir, resultBaseDir);
    return runner.run(SitespeedConstants.SITESPEED_IO_JUNIT_SCRIPT, args, env, logStream);

  }

  private int runSitespeed(String resultBaseDir, EnvVars env, PrintStream logStream)
      throws InterruptedException {
    BashRunner runner = new BashRunner(home);
    ParameterHelper paramHelper = new ParameterHelper(logStream);
    List<String> args = paramHelper.getSitespeedParameters(sitespeedConfiguration, resultBaseDir);
    return runner.run(SitespeedConstants.SITESPEED_IO_SCRIPT, args, env, logStream);
  }

  private boolean sendToGraphite(String fullOutputDir, PrintStream logStream) {

    File dateDir = new File(fullOutputDir);
    File pagesDir = new File(dateDir.getAbsolutePath() + "/data/pages");
    File metricsDir = new File(dateDir.getAbsolutePath() + "/data/metrics");
    File summaryDir = new File(dateDir.getAbsolutePath() + "/data");

    ReadSitespeedXMLFiles readSitepseedXmlFiles = new ReadSitespeedXMLFilesJDOM();
    Set<Page> pages = readSitepseedXmlFiles.getAnalyzedPages(pagesDir, logStream);
    Set<PageTimings> pageTimings = readSitepseedXmlFiles.getPageTimings(metricsDir, logStream);
    SiteSummary summary = readSitepseedXmlFiles.getSummary(summaryDir, logStream);

    ConvertToGraphite cg = new ConvertToGraphite(graphiteConfig);
    Map<String, Object> gdata = cg.get(pages, pageTimings, summary);
    GraphiteSender g = new GraphiteSender(graphiteConfig.getHost(), graphiteConfig.getPort());
    try {
      logStream.println("Sending data to Graphite ...");
      g.send(gdata);
      logStream.println("Data sent ok.");
      return true;

    } catch (UnknownHostException e) {
      logStream.println("Couldn't send data to Graphite:" + e.toString());
      return false;
    } catch (IOException e) {
      logStream.println("Couldn't send data to Graphite:" + e.toString());
      return false;
    }

  }

  private boolean validateInput(AbstractBuild<?, ?> build, PrintStream logStream) {

    // Right now the project name cannot contain any spaces
    if (build.getProject().getName().indexOf(" ") > -1) {
      logStream
          .println("The project name can't contain any spaces:" + build.getProject().getName());
      return false;
    }
    if (sitespeedConfiguration == null || "".equals(sitespeedConfiguration)) {
      logStream.println("You need to configure how sitespeed.io should run");
      return false;
    }

    return true;

  }

  private EnvVars getEnvironment(AbstractBuild<?, ?> build, BuildListener listener) {

    try {
      return build.getEnvironment(listener);
    } catch (IOException e1) {
      listener.getLogger().println("Couldn't fetch environment variables:" + e1);
      e1.printStackTrace();
    } catch (InterruptedException e1) {
      listener.getLogger().println("Couldn't fetch environment variables:" + e1);
      e1.printStackTrace();
    }

    return new EnvVars();
  }

  @Extension
  public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

    public static final String GRAPHITE_TEST_KEY = "sitespeed.io.test";

    public FormValidation doCheckConfiguration(@QueryParameter String value) throws IOException,
        ServletException {
      if (value.length() == 0)
        return FormValidation.error("You need to configure the sitespeed script");
      else
        return FormValidation.ok();
    }

    public FormValidation doCheckHome(@QueryParameter File value) throws IOException,
        ServletException {
      if (!Hudson.getInstance().hasPermission(Hudson.ADMINISTER)) {
        return FormValidation.ok();
      }
      
      if ("".equals(value.getPath()))
        return FormValidation.error("Please set the home dir");
      
      if (!value.isDirectory()) {
        return FormValidation.error("This is not a directory");
      }

      final File sitespeedExecutable = new File(value, "bin/sitespeed.io");
      if (!sitespeedExecutable.exists()) {
        return FormValidation.error("The sitespeed.io script doesn't exist:" + sitespeedExecutable.getAbsolutePath());
      }
      if (!sitespeedExecutable.canExecute()) {
        return FormValidation.error("The sitespeed.io script isn't executable");
      }
      return FormValidation.ok();
    }

    public FormValidation doCheckNamespace(@QueryParameter String value) throws IOException,
        ServletException {

      if (value.length() == 0)
        return FormValidation.error("Please set a namespace");
      else {
        if (value.matches("[a-zA-Z.]+"))
          return FormValidation.ok();
        else
          return FormValidation.error("The namespace needs to match [a-zA-Z.]+");
      }
    }

    public FormValidation doCheckPort(@QueryParameter String value) {
      if (value.length() > 0) {
        try {
          Integer val = Integer.parseInt(value);
          if (val > 1)
            return FormValidation.ok();
          else
            return FormValidation.error("The port need be a positive value");
        } catch (NumberFormatException e) {
          return FormValidation.error("The port need to be a number");
        }
      } else
        return FormValidation.ok();

    }

    public FormValidation doTestConnection(@QueryParameter("host") final String host,
        @QueryParameter("port") final String port) throws IOException, ServletException {

      GraphiteSender g = new GraphiteSender(host, Integer.parseInt(port));
      Map<String, Object> test = new HashMap<String, Object>();
      test.put(GRAPHITE_TEST_KEY, 1);
      try {
        g.send(test);
        return FormValidation.ok("Test data sent to Graphite with key:" + GRAPHITE_TEST_KEY);
      } catch (Exception e) {
        return FormValidation.error("Couldn't send data to Graphite: " + e.getMessage());
      }

    }

    public FormValidation doValidateHomeDir(@QueryParameter("home") final String home)
        throws IOException, ServletException {
      String sitespeedHome = home;
      if (home != null && !home.endsWith(File.separator)) sitespeedHome = home + File.separator;

      File sitespeedScript = new File(sitespeedHome + SitespeedConstants.SITESPEED_IO_SCRIPT);
      if (sitespeedScript.exists())
        return FormValidation.ok("Setup ok");
      else
        return FormValidation.error("Couldn't find the sitespeed.io script in " + home);

    }

    @Override
    public String getDisplayName() {
      return "sitespeed.io";
    }

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> aClass) {
      return true;
    }
  }
}
