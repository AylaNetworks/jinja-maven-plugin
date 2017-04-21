# jinja-maven-plugin

This project combines [jinjava](https://github.com/HubSpot/jinjava) and [snakeyaml](https://bitbucket.org/asomov/snakeyaml) to compile jinja template files with variable defintions in a yaml file from a maven project.

## Note from will
I made a slight modification to support saltstack:
- added optional <saltstack>true</saltstack>
- this parm will copy all k,v pairs into to dict named 'pillar' and now the tmpl file can be identical to a saltstack file.

## Usage

In your project file do:

```
...
  <build>
    <plugins>
      <plugin>
        <groupId>com.ayla</groupId>
        <artifactId>jinja-maven-plugin</artifactId>
        <version>1.0</version>
        <configuration>
          <outputFile>path-to-outout.txt</outputFile>
          <templateFile>path-to-template.jinja</templateFile>
          <varFile>path-to-variables.yaml</varFile>
        </configuration>
      </plugin>
    </plugins>
  </build>
...
```

and then execute the goal using

```
mvn com.ayla:jinja-maven-plugin:1.0:renderjinja
```

or make it part of your compile phase:

```
  <build>
    <plugins>
      <plugin>
        <groupId>com.ayla</groupId>
        <artifactId>jinja-maven-plugin</artifactId>
        <version>1.0</version>
        <executions>
          <execution>
            <phase>compile</phase>
            <goals>
              <goal>renderjinja</goal>
            </goals>
          </execution>
        </executions>
        <configuration>
          <outputFile>path-to-out.txt</outputFile>
          <templateFile>path-to-template.jinja</templateFile>
          <varFile>path-to-variables.yaml</varFile>
        </configuration>
      </plugin>
    </plugins>
  </build>
```
