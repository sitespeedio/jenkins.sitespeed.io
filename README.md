# Jenkins plugin for sitespeed.io [![Build Status](https://travis-ci.org/sitespeedio/jenkins.sitespeed.io.png?branch=master)](https://travis-ci.org/sitespeedio/jenkins.sitespeed.io)

This is the official plugin for running sitespeed.io in Jenkins. Full documentation: http://www.sitespeed.io/documentation/#jenkinsplugin

## Functionality
- Test your site against sitespeed.io best practice web performance rules and metrics from the Navigation Timing API
- Break builds using JUnit (both for rule score & Navigation Timing metrics)
- Choose which data you want to send to Graphite:
  - Sitespeed.io rule score per page
  - Navigation Timing API & User Timings interval metrics per page
  - Page metrics (like number of javascripts, css and images) per page
  - Summary of the rules score & Navigation Timing API metrics for all tested pages

## How to run in Jenkins
- Build the HPI file (not included in Jenkins automatically yet)
- Install the HPI file in Jenkins
- Download the latest version of sitespeed.io from http://www.sitespeed.io and put that on your Jenkins server
- Configure the plugin
- Run it!

## How to build the HPI file 
```
mvn package
```
and the file will be available in target/sitespeed.hpi

## How to build/run locally

```
mvn clean
mvn hpi:run -Djetty.port=8090
```
Access http://0.0.0.0:8090/jenkins/


