Diet Maven Mojo
============

The Diet Maven Mojo is a Maven Mojo that provides an easy to use interface to the [Diet Engine](https://github.com/codarama/Diet-engine) for projects that are built by Maven.

# Usage

## As a separate build goal
To include the plugin as a part of your build you would need to only add this to your pom.xml file :

```xml
<build>
	<plugins>
		<plugin>
			<groupId>org.codarama.diet</groupId>
			<artifactId>diet-maven-mojo</artifactId>
			<version>1.0.0</version>
		</plugin>
	</plugins>
</build>
```
, and then execute the goal as follows :

```
mvn clean diet:putondiet install
```

## As part of some of the existing phases
If you are bold enough you could simply put the plugin as part of one of the existing phases : 

```xml
<build>
	<plugins>
		<plugin>
			<groupId>org.codarama.diet</groupId>
			<artifactId>diet-maven-mojo</artifactId>
			<version>1.0.0</version>
			<inherited>true</inherited>
			<executions>
				<execution>
					<phase>package</phase>
					<goals>
						<goal>putondiet</goal>
					</goals>
				</execution>
			</executions>
		</plugin>
	</plugins>
</build>
```

, and then executing any goal that includes the package phase would also execute the mojo :

```
mvn clean install
```

## Configuration

```xml
<build>
	<plugins>
		<plugin>
			<groupId>org.codarama.diet</groupId>
			<artifactId>diet-maven-mojo</artifactId>
			<version>1.0.0</version>
			<inherited>true</inherited>
			<executions>
				<execution>
					<phase>package</phase>
					<goals>
						<goal>putondiet</goal>
					</goals>
				</execution>
			</executions>
			<configuration>
				<sources>/home/root/project/sources</sources>
				<libs>/usr/local/jdk/libs</libs>
				<target>/home/root/project/target</target>
				<forceInclude>
					<lib>/usr/local/jdk/libs/myRuntimeLibrary1.jar</lib>
					<lib>/usr/local/jdk/libs/myRuntimeLibrary2.jar</lib>
					<lib>/usr/local/jdk/libs/myRuntimeLibrary3.jar</lib>
				</forceInclude>
			</configuration>
		</plugin>
	</plugins>
</build>
```
Where:
* sources - overrides the source directory that Maven would supply with a custom sources directory to inspect
* libs - overrides the directory where the dependencies of the sources should be located
* target - overrides the target directory where the minimized JAR file will be placed
* forceInclude - provides a list of libraries to be forcefully included in the minimized JAR, because they are runtime libraries or there is some other reason the logic of the minimizer could not find them

# Notes
