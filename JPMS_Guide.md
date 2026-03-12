# Creating a Multi-Module Java Platform Module System Project with Maven
This document outlines the process of creating a multi-module Java Platform Module System (JPMS) project using Maven. 
Maven doesn’t offer a single archetype specifically for this, so we’ll create a parent POM and add modules manually.

## 1 Creating the Parent POM
First, create the parent project using the pom-root:

``` 
mvn archetype:generate
DarchetypeGroupId=org.codehaus.mojo.archetypes
DarchetypeArtifactId=pom-root
DarchetypeVersion=RELEASE 
```

Enter following in command line:
``` 
Define value for property ’groupId’: dk.sdu.sem4
Define value for property ’artifactId’: Gift_Basket_Production
Define value for property ’version’ 1.0-SNAPSHOT:
Define value for property ’package’ dk.sdu.sem4:
Confirm properties configuration:
groupId: dk.sdu.sem4
artifactId: Gift_Basket_Production
version: 1.0-SNAPSHOT
Y:
```

## 2 Modifying the Parent POM (pom.xml)
Change directory to the parent directory.
> cd Gift_Basket_Production

Edit the pom.xml file, changing the packaging to pom and add dependency-Management, build and profiles tags:
```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>dk.sdu.sem4</groupId>
    <artifactId>Gift_Basket_Production</artifactId>
    <version>1.0.1-SNAPSHOT</version>
    <packaging>pom</packaging>
    <name>Gift_Basket_Production</name>
    <repositories>
    </repositories>
        
    <properties>
        <java>java</java>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>        
        <maven.compiler.release>21</maven.compiler.release>
        <spring.version>6.1.3</spring.version>
    </properties>
    
    <dependencyManagement>
        <dependencies>
        </dependencies>
    </dependencyManagement>   
    
    <build>
        <plugins>
            <plugin>
                <artifactId>maven-clean-plugin</artifactId>
                <version>3.2.0</version>
            </plugin>
            <plugin>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.10.1</version>
                <configuration>
                    <fork>true</fork>
                </configuration>
            </plugin>
            <!-- execute the resulting project -->
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <version>3.5.0</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>exec</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <executable>java</executable>
                    <workingDirectory>.</workingDirectory>
                    <arguments>
                        <argument>--module-path=mods-mvn</argument>
                        <argument>--class-path=libs/*</argument>
                        <argument>-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=127.0.0.1:8000</argument>
                        <argument>--module=Core/dk.sdu.sem4.main.Main</argument>
                        <argument>--add-modules javafx.graphics</argument>
                    </arguments>
                </configuration>
            </plugin>
        </plugins>
    </build>

    <profiles>
        <profile>
            <!-- parent project should delete 'mods-mvn' -->
            <id>parent-project</id>
            <activation>
                <file>
                    <missing>src</missing>
                </file>
            </activation>
            <build>
                <plugins>
                    <plugin>
                        <artifactId>maven-clean-plugin</artifactId>
                        <version>3.2.0</version>
                        <configuration>
                            <filesets>
                                <fileset>
                                    <directory>mods-mvn</directory>
                                </fileset>
                                <fileset>
                                    <directory>libs</directory>
                                </fileset>
                            </filesets>
                        </configuration>
                    </plugin>
                </plugins>
            </build>
        </profile>
        <profile>
            <!-- child projects should copy artifact into 'mods-mvn' -->
            <id>child-project</id>
            <activation>
                <file>
                    <exists>src</exists>
                </file>
            </activation>
            <build>
                <plugins>
                    <plugin>
                        <artifactId>maven-dependency-plugin</artifactId>
                        <version>3.0.2</version>
                        <executions>
                            <execution>
                                <id>copy-installed</id>
                                <phase>install</phase>
                                <goals>
                                    <goal>copy</goal>
                                </goals>
                                <configuration>
                                    <artifactItems>
                                        <artifactItem>
                                            <groupId>${project.groupId}</groupId>
                                            <artifactId>${project.artifactId}</artifactId>
                                            <version>${project.version}</version>
                                            <type>jar</type>
                                        </artifactItem>
                                    </artifactItems>
                                    <outputDirectory>../mods-mvn</outputDirectory>
                                </configuration>
                            </execution>
                        </executions>
                    </plugin>
                </plugins>
            </build>
        </profile>
    </profiles>
</project>

```

## 3 Creating the Modules
From the root folder run:
```
mvn archetype:generate
DarchetypeGroupId=org.codehaus.mojo.archetypes
DarchetypeArtifactId=pom-root
DarchetypeVersion=RELEASE
```

Enter the following in the command line:
```
Define value for property ’groupId’: dk.sdu.sem4
Define value for property ’artifactId’: Core
Define value for property ’version’ 1.0 - SNAPSHOT:
Define value for property ’package’ dk.sdu.sem4:
Confirm properties configuration:
javaCompilerVersion: 17
junitVersion: 5.11.0
groupId: dk.sdu.sem4
artifactId: Core
version: 1.0 - SNAPSHOT
package: dk.sdu.sem4
Y:
```

Open the generated pom.xml file and change the content of the packaging tag to jar.
```
<project>
...
<packaging>jar</packaging>
...
</project>
```

## 4 Creating the Main Module
Create a main App.java class in the Core source folder:
>../Gift_Basket_Production/Core/src/main/java/dk/sdu/sem4

## 5 Building the Project
From the parent project directory, run:
> mvn clean install

## 6 Execute the Project
Run following java command from the root project folder:
>java --module -path mods-mvn --class-path " libs/*" -- module=Core/dk.sdu.sem4.App