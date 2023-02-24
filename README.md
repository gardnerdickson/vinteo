# Vinteo
Index, search for, and play videos on local and shared drives.

## Development

- Developed with Java 8
- Requires Maven 3
- Import the maven project to install dependencies and plugins
- To build the project, execute the `jfx-build` maven plugin: `mvn jfx:build-jar -f pom.xml`
    - This will create an executable jar and `lib` directory at `<repository_root>/target/jfx/app`
- Before running the application you must create a [configuration properties file](#configuration-file). The path to the configuration file can be passed in as a command line argument. If a path is not supplied, the application will look for a `config.properties` file in the current working directory. 
- To run the application: `java -jar <repository_root>/target/jfx/app/vinteo-x.x-SNAPSHOT-jfx.jar [path/to/config.properties]`

## Running from a Release

- Download and extract the [Latest Release](https://github.com/gardnerdickson/vinteo/releases)
- For Linux, run the `vinteo.sh` file.
- For Windows, run the `vinteo.cmd` file.
- If there are compatibility issues with your default Java version, you may need to download a Java 8 distribution and modify the contents of `vinteo.sh` or `vinteo.cmd` accordingly. 

## Configuration File

The structure of the properties file is as follows:
```properties
# The application will store user settings in this file.
usersettings.file=/path/to/user-settings.json
# The path to the sqlite database file. It should not exist when the application is run for the first time. 
sqlite.file=/path/to/storage.db
# The location of your VLC executable
command.vlc=/path/to/vlc
# Ephemeral files that application creates
temp.dir=/path/to/temp
# Keep-alive interval for network connected drives
connectionCheck.interval.ms=5000
```
