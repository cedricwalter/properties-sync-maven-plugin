
# Introduction
Apache maven plugin for **failing fast** at build time when using maven resources filtering with environment based `<filters>`

## Scenario
* You use maven filtering: some files get filtered because they may contains stage dependent values for example.
* you have some properties files that are used, and need to be kept in sync with another reference.properties.

This maven plugin will force you to keep in sync all properties files with the reference. 

**In Short it compare files against a reference file and enforce consistency.**


# Quick Usage
```<build>
<plugins>
    <plugin>
        <groupId>com.cedricwalter.maven.properties</groupId>
        <artifactId>properties-sync-maven-plugin</artifactId>
        <version>1.0</version>
        <executions>
            <execution>
                <goals>
                    <goal>properties</goal>
                </goals>
                <phase>process-resources</phase>
                <configuration>
                    <folder>${basedir}/src/main/resources</folder> <!-- optional here for sake of example -->
                    <reference>reference.properties</reference> <!-- optional here for sake of example -->
                    <pattern>[a-zA-Z]*.properties</pattern> <!-- optional here for sake of example -->
                </configuration>
            </execution>
        </executions>
    </plugin>
</plugins>
```
    
# Complete example

Imagine you have a folder src/main/filters which contains
* localhost.properties
* host1.properties

You want to force developer to keep in sync localhost.properties and host1.properties

Example of filtering
```
<build>              
    <filters>
        <filter>${basedir}/src/main/filters/${hostname}.properties</filter>                   
    </filters>
    
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-resources-plugin</artifactId>
            <version>3.0.1</version>
            <executions>
                <execution>
                    <id>filter-resources</id>
                    <phase>process-resources</phase>
                    <goals>
                        <goal>resources</goal>
                    </goals>
                    <configuration>
                        <nonFilteredFileExtensions>
                            <nonFilteredFileExtension>pdf</nonFilteredFileExtension>
                            <nonFilteredFileExtension>zip</nonFilteredFileExtension>
                            <nonFilteredFileExtension>jks</nonFilteredFileExtension>
                            <!-- ... list them all of binary will be broken by maven filtering -->
                            <nonFilteredFileExtension>jar</nonFilteredFileExtension>
                            <nonFilteredFileExtension>tar.gz</nonFilteredFileExtension>
                        </nonFilteredFileExtensions>
                    </configuration>
                </execution>
            </executions>
        </plugin>
        <plugin>
                <groupId>com.cedricwalter.maven.properties</groupId>
                <artifactId>properties-sync-maven-plugin</artifactId>
                <version>1.0</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>properties</goal>
                        </goals>
                        <phase>process-resources</phase>
                        <configuration>
                            <folder>${basedir}/src/main/filters</folder> 
                            <reference>localhost.properties</reference> 
                            <pattern>host1.properties</pattern>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
    </plugins>
</build>
```

The build will now failed if both files do not contains the same number of keys.