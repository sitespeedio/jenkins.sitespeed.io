# Jenkins plugin for sitespeed.io [![Build Status](https://travis-ci.org/sitespeedio/jenkins.sitespeed.io.png?branch=master)](https://travis-ci.org/sitespeedio/jenkins.sitespeed.io)

This is the official plugin for running sitespeed.io in Jenkins ([documentation]( http://www.sitespeed.io/documentation/#jenkinsplugin)).

Current status: When the plugin can run as a slave the 1.0 will be released.

## Functionality
- Test your site against sitespeed.io best practice web performance rules and metrics from the Navigation Timing API
- Break builds using JUnit/TAP or built in budget.
- Send all the metrics to Graphite.
- Test using [WebPageTest](http://www.webpagetest.org)

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


