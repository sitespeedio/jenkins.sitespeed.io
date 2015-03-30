/**
 * Sitespeed.io - How speedy is your site? (http://www.sitespeed.io) Copyright (c) 2014, Peter
 * Hedenskog, Tobias Lidskog and other contributors Released under the Apache 2.0 License
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
import hudson.util.ListBoxModel;
import io.sitespeed.jenkins.BashRunner;
import io.sitespeed.jenkins.ParameterHelper;
import io.sitespeed.jenkins.SitespeedConstants;
import io.sitespeed.jenkins.SitespeedLinkAction;
import io.sitespeed.jenkins.configuration.ExecutablePathConfiguration;
import io.sitespeed.jenkins.configuration.ExtraConfiguration;
import io.sitespeed.jenkins.configuration.GraphiteConfiguration;
import io.sitespeed.jenkins.configuration.WPTConfiguration;
import io.sitespeed.jenkins.graphite.GraphiteSender;
import io.sitespeed.jenkins.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class SitespeedBuilder extends Builder {

  /**
   * The configuration sent to the sitespeed script.
   */
  private final ExtraConfiguration extraConfiguration;

  /**
   * The limit configutration for budget/TAP/JUnit.
   */
  private final String budget;

  /**
   * The home the sitespeed executable.
   */
  private final String sitespeedExecutable;

  /**
   * Which browser to use.
   */
  private final String browser;


  /**
   * How many runs in each browser.
   */
  private final Integer runs;

  /**
   * If Graphite is checked or not.
   */
  private final boolean checkGraphite;

  /**
   * If the extra configuration box is checked or not.
   */
  private final boolean checkExtraConfiguration;

  /**
   * If the path to the sitespeed binary box is checked or not.
   */
  private final boolean checkPath;

  /**
   * If the checkbox for WebPagetTest is checked or not.
   */
  private final boolean checkWpt;

  /**
   * Configuration for Graphite.
   */
  private GraphiteConfiguration graphiteConfig;

  /**
   * The URL:s to test.
   */
  private final String urls;

  /**
   * How deep we wanna crawl.
   */
  private final Integer crawlDepth;

  /**
   * The configuration for WebPageTest.
   */
  private final WPTConfiguration wptConfig;

  /**
   * The output type JUnitXML/TAP or budget
   */
  private final String output;

  private final String defaultBudget = "{\"rules\": \n{ \"default\": 90 }\n}";

  /**
   * The URL to the sitespeed logo.
   */
  private static final String ICON_URL = "/plugin/sitespeed/logo48x48.png";

  @DataBoundConstructor
  public SitespeedBuilder(ExtraConfiguration checkExtraConfiguration, String budget,
      ExecutablePathConfiguration checkPath, String output,
      GraphiteConfiguration checkGraphite, String urls, Integer crawlDepth, String browser,
      Integer runs, WPTConfiguration checkWpt) {

    extraConfiguration = checkExtraConfiguration;
    
    sitespeedExecutable =
        checkPath == null ? "sitespeed.io" : checkPath.getSitespeedExecutable();
    this.budget = "".equals(budget) || budget == null ? defaultBudget : budget;
    wptConfig = checkWpt;
    this.urls = urls;
    this.browser = browser;
    this.runs = runs;
    graphiteConfig = checkGraphite;
    this.checkGraphite = checkGraphite == null ? false : true;
    this.checkExtraConfiguration = extraConfiguration == null ? false : true;
    this.checkPath = checkPath == null ? false : true;
    this.checkWpt = wptConfig == null ? false : true;
    this.output = "".equals(output) ? "junit" : output;
    this.crawlDepth = crawlDepth == null ? 0 : crawlDepth;
     
  }

  @Override
  public DescriptorImpl getDescriptor() {
    return (DescriptorImpl) super.getDescriptor();
  }

  public String getSitespeedExecutable() {
    return sitespeedExecutable;
  }
  
  public String getSitespeedConfiguration() {
    return extraConfiguration!=null?extraConfiguration.getSitespeedConfiguration():"";
  }

  public String getWptKey() {
    return  wptConfig!=null?wptConfig.getWptKey():"";
  }
  
  public String getWptHost() {
    return  wptConfig!=null?wptConfig.getWptHost():"";
  }
  
  public String getWptConfig() {
    return  wptConfig!=null?wptConfig.getWptConfig():"";
  }
  
  public int getCrawlDepth() {
    return crawlDepth;
  }

  public String getBrowser() {
    return browser;
  }

  public int getRuns() {
    return runs;
  }

  public String getOutput() {
    return output;
  }

  public String getUrls() {
    return urls;
  }

  public String getHost() {
    return graphiteConfig != null ? graphiteConfig.getHost() : "";
  }

  public String getBudget() {
    return budget;
  }

  public String getNamespace() {
    return graphiteConfig != null ? graphiteConfig.getNamespace() : "";
  }

  public String getPort() {
    return graphiteConfig != null ? graphiteConfig.getPort() + "" : "";
  }


  public boolean isCheckGraphite() {
    return checkGraphite;
  }

  public boolean isCheckPath() {
    return checkPath;
  }

  public boolean isCheckWpt() {
    return checkWpt;
  }

  public boolean isCheckExtraConfiguration() {
    return checkExtraConfiguration;
  }

  @Override
  public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {

    EnvVars env = getEnvironment(build, listener);
    PrintStream logStream = listener.getLogger();

    // simple check that everything is setup ok.
    if (!validateInput(build, logStream)) return false;

    File sitespeedOutputDir = FileUtil.getInstance().getSitespeedOutputDir(build);

    try {
      // First run sitespeed
      int returnValue = runSitespeed(sitespeedOutputDir.getAbsolutePath(), env, logStream, build);
      String domainDirName =
          FileUtil.getInstance().getLastModifiedFileNameInDir(sitespeedOutputDir);

      String dateDirName =
          FileUtil.getInstance().getLastModifiedFileNameInDir(
              FileUtil.getInstance().getLastModifiedFileInDir(sitespeedOutputDir));

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


  private int runSitespeed(String resultBaseDir, EnvVars env, PrintStream logStream,
      AbstractBuild<?, ?> build) throws InterruptedException {
    BashRunner runner = new BashRunner(sitespeedExecutable);
    ParameterHelper paramHelper = new ParameterHelper(logStream);
    List<String> args =
        paramHelper.getSitespeedParameters(
            (extraConfiguration != null ? extraConfiguration.getSitespeedConfiguration() : ""),
            resultBaseDir);
    
    
    String filename = addSitespeedParameters(args, build, logStream);

    return runner.run(args, env, logStream, filename);
  }
  
  private String addSitespeedParameters(List<String> args, AbstractBuild<?, ?> build, PrintStream logStream) {
 
    String filename = "budgetresult.txt";
    
    // add specific pararmeters to sitespeed.
    if ("junit".equals(output)) {
      args.add("--junit");
      filename = new String(build.getWorkspace().getRemote() + "/" + "sitespeed.io-junit.xml");
    } else if ("tap".equals(output)) {
      args.add("--tap");
      new String(build.getWorkspace().getRemote() + "/" + "sitespeed.io-junit.tap");
    }

    String[] theUrls = urls.split("\n");
    if (theUrls.length == 1) {
      args.add("-u");
      args.add(theUrls[0]);
    } else {
      FileUtil.getInstance().storeFile(build.getWorkspace().getRemote() + "/" + "urls.txt", urls,
          logStream);
      args.add("-f");
      args.add(build.getWorkspace().getRemote() + "/" + "urls.txt");
    }

    FileUtil.getInstance().storeFile(build.getWorkspace().getRemote() + "/" + "budget.json",
        budget, logStream);
    args.add("--budget");
    args.add(build.getWorkspace().getRemote() + "/" + "budget.json");

    args.add("-d");
    args.add(crawlDepth.toString());

    if (!"".equals(browser)) {
      args.add("-b");
      args.add(browser);
      args.add("-n");
      args.add(runs.toString());
    }

    if (wptConfig != null) {
      if (!"".equals(wptConfig.getWptHost())) {
        args.add("--wptHost");
        args.add(wptConfig.getWptHost());
      }
      if (!"".equals(wptConfig.getWptKey())) {
        args.add("--wptKey");
        args.add(wptConfig.getWptKey());
      }
      if (!"".equals(wptConfig.getWptConfig())) {
        args.add("--wptConfig");
        args.add(wptConfig.getWptConfig());
      }
    }

    if (graphiteConfig != null) {
      args.add("--graphiteHost");
      args.add(graphiteConfig.getHost());
      args.add("--graphiteNamespace");
      args.add(graphiteConfig.getNamespace());
      args.add("--graphitePort");
      args.add(graphiteConfig.getPort() + "");

    }
    return filename;
    
  }

  private boolean validateInput(AbstractBuild<?, ?> build, PrintStream logStream) {

    // Right now the project name cannot contain any spaces
    if (build.getProject().getName().indexOf(" ") > -1) {
      logStream
          .println("The project name can't contain any spaces:" + build.getProject().getName());
      return false;
    }

    if (urls == null || "".equals(urls)) {
      logStream.println("You need to configure the urls to test");
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

    public ListBoxModel doFillCrawlDepthItems() {
      ListBoxModel items = new ListBoxModel();
      items.add("Don't crawl", "0");
      items.add("1", "1");
      items.add("2", "2");
      items.add("3", "3");
      return items;
    }

    public ListBoxModel doFillBrowserItems() {
      ListBoxModel items = new ListBoxModel();
      items.add("No browser", "");
      items.add("Chrome", "chrome");
      items.add("Firefox", "firefox");
      items.add("Safari (8 or later)", "safari");
      return items;
    }

    public ListBoxModel doFillRunsItems() {
      ListBoxModel items = new ListBoxModel();
      for (int i = 1; i < 24; i++)
        items.add(i + "", i + "");
      return items;
    }

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

      if (!value.canExecute()) {
        return FormValidation.error("The sitespeed.io binary isn't executable for Jenkins");
      }
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

    public FormValidation doValidateBinary(@QueryParameter("sitespeedExecutable") final String home)
        throws IOException, ServletException {

      if ("sitespeed.io".equals(home)) {
       try {
        Runtime.getRuntime().exec("sitespeed.io --version");
        return FormValidation.ok("Setup ok");
       }
       catch (Exception e) {
         return FormValidation.error("Couldn't find the sitespeed.io binary " + home);
       }
      }
      else {
      File sitespeedScript = new File(home);
      if (sitespeedScript.exists())
        return FormValidation.ok("Setup ok");
      else
        return FormValidation.error("Couldn't find the sitespeed.io binary " + home);
      }

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
