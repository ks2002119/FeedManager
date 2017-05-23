email_processing
==============

## Requirements
- Java 8, Maven 3.x

## Running unit tests
- `mvn test`: Runs the unit tests relevant to this project
  

## Build & Packaging 
- `mvn package`: Packages the artifact that can be run.

## Configuration
conf directory holds the application.properties file. This file can be used to change feed reader 
application configuration. 


## Steps to run the application

To build the artifact, run `mvn package -DskipTests` command.
To launch analyzer, run `java -jar target/feedsystem-jar-with-dependencies.jar`
Note: Launch the java application from the root directory of the project if you are not going to 
pass any command line args. If you are launching the application from any other location, please 
pass the appropriate conf directory that contains all the configuration files. By default, 
application runs at port 4567.


