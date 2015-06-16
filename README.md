Very simple coffeescript maven plugin. It can merge and convert all the coffeescript files.

# Sample usage

```xml
<build>
  ...
  <plugins>
     ...
     <plugin>
        <groupId>com.github.wpic</groupId>
        <artifactId>coffeescript-maven-plugin</artifactId>
        <version>1.0-SNAPSHOT</version>
        <configuration>
           <outputFile>${project.build.directory}/js/all.js</outputFile>
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

# Parameters

* inputDir: Source directory contains coffeescript files. ```src/main/webapp``` by default.
* outputDir: Destination directory to save. ```target/``` by default.
* outputFile: If you set this parameter, all the coffeescripts will merge into the single file (outputDir will be ignore)