/**
 * Sitespeed.io - How speedy is your site? (http://www.sitespeed.io)
 * Copyright (c) 2014, Peter Hedenskog, Tobias Lidskog
 * and other contributors
 * Released under the Apache 2.0 License
 */
package io.sitespeed.jenkins;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class for cleaning up arguments sent to sitespeed.
 * 
 */
public class ParameterHelper {

  private static final Pattern SPLIT_CLI_ARGUMENTS_REGEXP = Pattern
      .compile("[^\\s\"']+|\"[^\"]*\"|'[^']*'");
  private static final Set<String> SITESPEED_PARAMETERS_TO_REMOVE = new HashSet<String>(
      Arrays.asList("-r"));
  private static final Set<String> SITESPEED_JUNIT_PARAMETERS_TO_REMOVE = new HashSet<String>(
      Arrays.asList("-r", "-o"));

  private final PrintStream logStream;

  public ParameterHelper(PrintStream logStream) {
    this.logStream = logStream;
  }

  public List<String> getSitespeedParameters(String configuration, String resultBaseDir) {
    LinkedList<String> argList = getArgsAsList(configuration);
    removeUnwantedParameters(argList, SITESPEED_PARAMETERS_TO_REMOVE, logStream);
    argList.add("-r");
    argList.add(resultBaseDir);
    return argList;
  }

  public List<String> getJUnitParamters(String configuration, String outputDir, String resultBaseDir) {
    LinkedList<String> argList = getArgsAsList(configuration);
    removeUnwantedParameters(argList, SITESPEED_JUNIT_PARAMETERS_TO_REMOVE, logStream);
    argList.add("-o");
    argList.add(outputDir);
    argList.add("-r");
    argList.add(resultBaseDir);
    return argList;
  }

  private LinkedList<String> getArgsAsList(String args) {
    LinkedList<String> matchList = new LinkedList<String>();
    Matcher regexMatcher = SPLIT_CLI_ARGUMENTS_REGEXP.matcher(args);
    while (regexMatcher.find()) {
      matchList.add(regexMatcher.group());
    }
    return matchList;
  }

  private void removeUnwantedParameters(LinkedList<String> args, Set<String> argsToRemove,
      PrintStream logStream) {
    for (String paramToRemove : argsToRemove) {
      if (args.contains(paramToRemove)) {
        // remove the argument and the value
        int i = args.indexOf(paramToRemove);
        String value = args.remove(i + 1);
        args.remove(i);
        logStream
            .println("You can't change output and resultBaseDir when running from Jenkins. Removing parameter: "
                + paramToRemove + " with value:" + value);
      }
    }
  }

}
