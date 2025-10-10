## Axiomatics Policy Devops

This repository is the entry point to start with Attribute Base Access Control (ABAC) and Policy-as-code from Axiomatics.

## Documentation

Online documentation from Axiomatics is available at https://docs.axiomatics.com/axiomatics-policy-devops/

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
A simple policy example allowing all consultants to access resources in their own location.

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
### Test

A simple example test that verifies Cecilia can access resource 1. This test is a system tests so it assumes test PIP data that Cecilia is a consultant and located in the same location as resource 1.

```java
public class MySystemTest {

    @RegisterExtension
    public AlfaExtension alfa = new AlfaExtension().withAttributeConnectors();

    @Test
    public void shouldGiveCeciliaAccessToResource1() {
        TestRequest target = alfa.newTestRequest()
                .with("user.identity", "cecilia")
                .with("resource.identity", "1");

        TestResponse result = target.evaluate();

        assertThat(result, is(permit()));
    }
}

```

#### Test trace visualisation

After test run, a visualization trace can be produced. It shows how the policy was evaluated and which attributes were fetched from PIPs. This gives an overview of the policy or when detail mode is enabled, evaluation result of any function is shown to support debugging. Note that test trace visualisation is currently released in as a preview feature and is not covered by SLA.

##### High level policy evaluation view
![shouldGiveCeciliaAccessToResource1](https://github.com/user-attachments/assets/82da82b9-7c82-45ff-89d6-c01ad77c4b25)

##### Detailed level policy evaluation view
![detaild_shouldGiveCecilaAccessToResource1](https://github.com/user-attachments/assets/1131f438-1073-4d21-9d9f-75ac3db7a1cc)

##### PIP result view
![attributes](https://github.com/user-attachments/assets/7ec6bb5e-4b47-4fbc-9af1-7e7a197374ac)



## Contact
Feel free to contact us at https://www.axiomatics.com if you have any questions



