# interlok-project-converter

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

```
$ ./build/staged/bin/interlok-project-conveter  ./interlok-continuous-delivery/src/main/dist/config/adapter.xml ./interlok-continuous-delivery/src/main/dist/config/variables.properties
Written to [C:\repo\code\adaptris\other\interlok-project-converter\.\build\staged\config-project.json]
```
