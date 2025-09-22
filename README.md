## Axiomatics Policy Devops

A project that will _[add your own description of your project]._

## Documentation

Online documentation from Axiomatics is available at https://docs.axiomatics.com/axiomatics-policy-devops/

### Quick start

1. Add the Axiomatics repository access keys, that you received from Axiomatics, in file `gradle.properties`
2. Make sure your IDE, such as IntelliJ or Visual Studio Code has the necessary plugins for Java and Gradle projects
3. Open this project in your IDE, it contains a sample project
4. Execute gradle task `test` to test your policies and attribute connectors
5. Execute gradle task`buildAuthzDomain` to build the authorization domain into file `build/alfa/domain/ads/domain.yaml`
6. Copy Axiomatics ADS license, that you received from Axiomatics, to `license/` directory
7. Execute gradle task `runAds` to start the Access Decision Service locally
8. Execute `buildAdsDockerImage` to build a docker image to the local docker registry
