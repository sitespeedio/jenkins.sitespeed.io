/**
 * Sitespeed.io - How speedy is your site? (http://www.sitespeed.io) Copyright (c) 2014, Peter
 * Hedenskog, Tobias Lidskog and other contributors Released under the Apache 2.0 License
 */
package io.sitespeed.jenkins;

import hudson.EnvVars;
import io.sitespeed.jenkins.util.FileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Run commands using Bash.
 * 
 * @author Peter Hedenskog.
 * 
 */
public class BashRunner {

  private final String executable;

  /**
   * Create the class. Tell which executable to run.
   * 
   * @param executable is the executable to run.
   */
  public BashRunner(String executable) {
    this.executable = executable;
  }

  /**
   * Run the executable.
   * 
   * @param args the argument to the executable.
   * @param env the environment vars from Jenkins.
   * @param stream the Jenkins log stream
   * @param outputFile the file where the output from the command will be written. If null, no file
   *        is created.
   * @return
   * @throws InterruptedException
   */
  public int run(List<String> args, EnvVars env, PrintStream stream, String outputFile)
      throws InterruptedException {

    args = translateEnvInArgs(args, env);
    try {
      List<String> processArguments = new LinkedList<String>();
      processArguments.add(executable);
      processArguments.addAll(args);

      stream.println("Will run executable" + this.executable + " with arguments: "
          + processArguments);

      ProcessBuilder pb = new ProcessBuilder(processArguments);

      // Copy build env to the process, fix the DISPLAY env
      Map<String, String> processEnv = pb.environment();

      if (env != null) {
        for (String key : env.keySet()) {
          processEnv.put(key, env.get(key));
        }
      }


      pb.redirectErrorStream(true);
      Process p = pb.start();

      BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
      StringBuilder output = new StringBuilder();
      String line = "";
      while ((line = input.readLine()) != null) {
        stream.println(line);
        output.append(line);
      }

      input.close();

      if (outputFile != null) {
        FileUtil.getInstance().storeFile(outputFile, output.toString(), stream);
      }
      return p.waitFor();
    } catch (IOException e) {
      stream.println(e.toString());
      return -1;
    }

  }


  private List<String> translateEnvInArgs(List<String> args, EnvVars env) {
    if (env != null) {
      List<String> modifiedArgs = new LinkedList<String>();
      for (String argument : args) {
        for (String key : env.keySet()) {
          argument = argument.replaceAll(Matcher.quoteReplacement("$" + key), env.get(key));
        }
        modifiedArgs.add(argument);
      }
      return modifiedArgs;
    } else
      return args;
  }

}
