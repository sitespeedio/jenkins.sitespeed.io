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
 * @author peter
 * 
 */
public class BashRunner {

  private final String homeDir;

  /**
   * 
   * @param homeDir is the home of the script/command to run.
   */
  public BashRunner(String homeDir) {
    this.homeDir = homeDir;
  }

  public int run(String command, List<String> args, EnvVars env, PrintStream stream)
      throws InterruptedException {

    args = translateEnvInArgs(args, env);
    try {
      List<String> processArguments = new LinkedList<String>();
      processArguments.add(command);
      processArguments.addAll(args);

      ProcessBuilder pb = new ProcessBuilder(processArguments);

      // Copy build env to the process, fix the DISPLAY env
      Map<String, String> processEnv = pb.environment();

      if (env != null) {
        for (String key : env.keySet()) {
          processEnv.put(key, env.get(key));
        }
      }

      pb.directory(new File(homeDir));
      pb.redirectErrorStream(true);
      Process p = pb.start();

      BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
      String line = "";
      while ((line = input.readLine()) != null) {
        stream.println(line);
      }

      input.close();
      return p.waitFor();
    } catch (IOException e) {
      stream.println(e.toString());
      return -1;
    }
  }

  private List<String> translateEnvInArgs(List<String> args, EnvVars env) {

    if (env != null) {
      List<String> modifiedArgs = new LinkedList<String>();
      for (String key : env.keySet()) {
        for (String argument : args) {
          modifiedArgs.add(argument.replaceAll(Matcher.quoteReplacement("$" + key), env.get(key)));
        }
      }
      return modifiedArgs;
    } else
      return args;
  }

}
