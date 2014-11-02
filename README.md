Diet Maven Mojo
============

The Diet Maven Mojo is a Maven Mojo that provides an easy to use interface to the [Diet Engine](https://github.com/codarama/Diet-engine) for projects that are built by Maven.

# Usage

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

# Notes
