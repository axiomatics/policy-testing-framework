## Axiomatics Alfa project

A project that will [add your description of project].

## How to build

For documentation see [extra/documentation/index.html](extra/documentation/index.html).

### Quick start

1. Add the Axiomatics repository access keys, that you received from Axiomatics, in gradle.properties
2. Make sure your IDE, such as IntelliJ or Visual Studio Code has the necessary plugins for Java and Gradle projects
3. Load this project in your IDE
4. Execute target `test` to test your policies and attribute connectors
5. Execute target `buildAuthzDomain` to build the project.
6. Copy Axiomatics ADS license, that you received from Axiomatics, to `license/` directory

Then the following options exists to run or publish your project:

* Execute target `runAds` to start the Access Decision Service locally
* Execute buildAdsDockerImage to build a docker image to the local docker registry
* Add a new task of type AdmPush in build.gradle to be able to push this project to domain repository in Axiomatics Policy Server 
 
