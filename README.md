# interlok-project-migrator [![Build Status](https://travis-ci.org/adaptris-labs/interlok-project-migrator.svg?branch=develop)](https://travis-ci.org/adaptris-labs/interlok-project-migrator) [![codecov](https://codecov.io/gh/adaptris-labs/interlok-project-migrator/branch/develop/graph/badge.svg)](https://codecov.io/gh/adaptris-labs/interlok-project-migrator) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/8ee663bc89d14733bce1e99ba1aa9ea6)](https://www.codacy.com/app/adaptris-labs/interlok-project-migrator?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=adaptris-labs/interlok-project-migrator&amp;utm_campaign=Badge_Grade)

Helper application that allows you to convert Interlok xml and variables into a project zip (introduced in 3.7).

## Build

```text
$ ./gradlew clean assemble
:clean
:compileJava
:processResources NO-SOURCE
:classes
:jar
:startScripts
:distTar SKIPPED
:distZip SKIPPED
:installDist
:assemble

BUILD SUCCESSFUL in 11s
5 actionable tasks: 5 executed
```

## Execute

```shell
./build/staged/bin/interlok-project-migrator -p MyInterlokInstance -a /c/interlok/config/adapter.xml -v /c/interlok/config/variables.properties
```

## Help

```text
$ ./build/staged/bin/interlok-project-migrator
Parsing failed.  Reason: Missing required options: a, v
usage: interlok-project-conveter
 -a,--adapter <arg>     (required) The adapter xml
 -h,--help              Displays this..
 -p,--project <arg>     The project name
 -v,--variables <arg>   (required) The variables (can be added multiple times)
```
