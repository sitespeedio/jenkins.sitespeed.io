# Jenkins plugin for sitespeed.io

This is the official plugin for running sitespeed.io in Jenkins. Note: You need to download sitespeed.io from http://www.sitespeed.io and put that on your server, to be able to run.

## Functionality
TBD

## How to build/run locally

```
mvn clean
mvn install
mvn hpi:run -Djetty.port=8090
```
Access http://0.0.0.0:8090/jenkins/


