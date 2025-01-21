## Axiomatics Policy Testing Framework

This repository is the entry point to start with Attribute Base Access Control (ABAC) and Policy-as-code from Axiomatics.

## Documentation

Online documentation from Axiomatics is available at https://docs.axiomatics.com/policy-testing-framework/

ALFA introduction available at https://axiomatics.github.io/alfa-vscode-doc/docs/alfa-introduction/introduction/

### Quick start

1. Add the Axiomatics repository access keys, that you received from Axiomatics, in file `gradle.properties`
2. Make sure your IDE, such as IntelliJ or Visual Studio Code has the necessary plugins for Java and Gradle projects
3. Open this project in your IDE, it contains a sample project
4. Execute gradle task `test` to test your policies and attribute connectors
5. Execute gradle task`buildAuthzDomain` to build the authorization domain into file `build/alfa/domain/ads/domain.yaml`
6. Copy Axiomatics ADS license, that you received from Axiomatics, to `license/` directory
7. Execute gradle task `runAds` to start the Access Decision Service locally
8. Execute `buildAdsDockerImage` to build a docker image to the local docker registry


### ALFA
A simple example allowing all consultants to access resources in their own location.

#### main.alfa
```groovy 

namespace acme {

    policyset Main {
        apply firstApplicable
        consultants.Main
    }
}

```

#### consultants.alfa
```groovy
namespace consultants {
    policy Main {
        target
            clause user.role == "consultant"
        
        apply firstApplicable
        rule permitIfLocationsMatch {
            permit
            condition user.location == resource.location
        }
    }
}
```
## Contact
Feel free to contact us at https://www.axiomatics.com if you have any questions
