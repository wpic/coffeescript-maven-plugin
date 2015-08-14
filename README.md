Very simple coffeescript maven plugin. It can merges and converts all the coffeescript files into a single file. This plugin use Nashorn engine for Java8 (much faster) also Java5+ fallback. So, use Java8 if you want to boost performance (at least 2 times faster).

# Sample usage

```xml
<build>
  ...
  <plugins>
     ...
     <plugin>
        <groupId>com.github.wpic</groupId>
        <artifactId>coffeescript-maven-plugin</artifactId>
        <version>1.0</version>
        <configuration>
           <outputFile>${project.build.directory}/${project.artifactId}/js/all.js</outputFile>
        </configuration>
        <executions>
           <execution>
              <phase>compile</phase>
              <goals>
                 <goal>coffeescript</goal>
              </goals>
           </execution>
        </executions>
     </plugin>
     ...
  </plugins>
</build>
```

# Use the last vesion

Snapshots are not available in maven repository, you can find them [here](https://oss.sonatype.org/content/repositories/snapshots/com/github/wpic/coffeescript-maven-plugin/).

# Parameters

* inputDir: Source directory contains coffeescript files. ```src/main/webapp``` by default.
* outputDir: Destination directory to save. ```target/``` by default.
* outputFile: If you set this parameter, all the coffeescripts will merge into the single file (outputDir will be ignore)

# History

**v1.1**
* Fix throwing exception on compile failure

**v1.1-SNAPSHOT**
* Boost performance (Use Nashorn support with Rihno fallback for old version of java)
* Add filters (Include/Exclude)
* Fix error handling and more bugs

**v1.0**
* First release
