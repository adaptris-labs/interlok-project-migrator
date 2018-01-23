# interlok-project-migrator

This project is helper that allows you to convert Interlok xml and variables into a project zip (introduced in 3.7).

## Build
```
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
```
./build/staged/bin/interlok-project-conveter -p MyInterlokInstance -a /c/interlok/config/adapter.xml -v /c/interlok/config/variables.properties
```

## Help
```
$ ./build/staged/bin/interlok-project-conveter
Parsing failed.  Reason: Missing required options: a, v
usage: interlok-project-conveter
 -a,--adapter <arg>     (required) The adapter xml
 -h,--help              Displays this..
 -p,--project <arg>     The project name
 -v,--variables <arg>   (required) The variables (can be added multiple times)
```
