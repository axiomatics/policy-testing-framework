package tasks

import extensions.AdmExtension
import extensions.AlfaExtension
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.jvm.Jvm

class RunSpawnedAds extends DefaultTask {


    @Input
    AlfaExtension config

    @Input
    AdmExtension adm

    @Input
    int admHash

    @InputFiles
    FileCollection classpath


    @TaskAction
    void run() {
        if (config == null || adm ==null) {
            throw new RuntimeException("Inputs can not be null")
        }
        String java = Jvm.current().getJavaExecutable().getAbsolutePath()
        logger.info( "Starting ADS with domain from ${adm.environment}")
        logger.info( "Java is ${java}")
        def cmds = new ArrayList<String>()
        def joinedClasspath = classpath.join("${File.pathSeparatorChar}")
        logger.info( "Classpath is ${joinedClasspath}")
        logger.info( "Environment is ${adm.envVariable}")
        cmds << java
        cmds << "-cp"
        cmds << joinedClasspath

        if (System.getProperty("javax.net.ssl.trustStore") != null) {
            cmds << "-Djavax.net.ssl.trustStore=" + System.getProperty("javax.net.ssl.trustStore")
        }
        if (System.getProperty("javax.net.ssl.trustStorePassword") != null) {
            cmds << "-Djavax.net.ssl.trustStorePassword=" + System.getProperty("javax.net.ssl.trustStorePassword")
        }
            if (System.getProperty("javax.net.ssl.trustStoreType") != null) {
                cmds << "-Djavax.net.ssl.trustStoreType=" + System.getProperty("javax.net.ssl.trustStoreType")
            }
        cmds << "com.axiomatics.ads.App"
        cmds << "server"

        def deploymentFile = adm.project.buildDir.toPath().resolve(this.name + "/deployment.yaml")
        deploymentFile.parent.toFile().mkdirs()
        def deploymentFileAsString = deploymentFile.toAbsolutePath().toString()
        logger.info( "Deployment file is ${deploymentFileAsString}")
        cmds << deploymentFileAsString

        if (adm.adsHttpPort == null) {
            ServerSocket socket = new ServerSocket(0)
            socket.close()
            int port=   socket.getLocalPort()
            if (port < 1) {
                throw new RuntimeException("Could not get local port")
            }
            adm.adsHttpPort = port
            logger.info("Obtain free port ${port}")
        } else {
            logger.lifecycle("Port overridden: " + adm.adsHttpPort)
        }
        def deploymentContent  = getDeploymentTemplate()
        logger.info("Deployment content:${deploymentContent} ")
        deploymentFile << deploymentContent

        ProcessBuilder builder = new ProcessBuilder(cmds);
        builder.environment() << adm.envVariable
        builder.redirectErrorStream(true)

        adm.process = builder.start()
    }
    private String getDomainUrl() {
        boolean isBasic = adm.basicCredentials != null
        def template
        if (isBasic) {
            template = adm.apiUrlPullStandaloneTemplate
        } else {
            template = adm.apiUrlPullAsmTemplate

        }
        def result = adm.host + String.format(template,adm.alfa.namespace,adm.domainName)

        return result
    }

    private String getAuthenticationYaml() {

      return getBasicAuthenticationYaml() +
         getOidcAuthenticationYaml()


    }

    @Internal
    String getDeploymentTemplate() {
        def url = getDomainUrl();
        logger.lifecycle("Location of domain.yaml is ${url}" )
        return """
          
license: ${adm.project.getProjectDir()}/${config.licenseFile}
domain: ${url}

audit:
  mode: verbose

${getAuthenticationYaml()}

authentication:
  type: Basic
  user:
    username: pdp-user
    #password is 'secret'
    hashPassword: 2bb80d537b1da3e38bd30361aa855686bde0eacd7162fef6a25fe97bf527a25b
server:
  adminConnectors: []
  applicationConnectors:
    - type: http
      port: ${adm.adsHttpPort}
  requestLog:
    appenders:
      - type: console
logging:
  level: WARN  
  loggers:
    "org.apache.jcs": \${LOGLEVEL:-${logger.isInfoEnabled()?"WARN":"INFO"}}
    "com.axiomatics": \${LOGLEVEL:-${logger.isInfoEnabled()?"DEBUG":"INFO"}}
  appenders:
    - type: console
      target: stdout
      timeZone: system
""" }

    @Internal
    String getBasicAuthenticationYaml() {
     """
httpClientConfiguration:
  ${getBasicCredentials()}
  ${getTlsConfig()}
"""
    }

    @Internal getBasicCredentials() {
        boolean isBasic = adm.basicCredentials != null
        if (isBasic) {

        return """
  domainUser: ${adm.basicCredentials.username}
  domainPassword: ${adm.basicCredentials.password}
  timeout: 30 seconds

"""
    } else {return ""}

    }
    @Internal
     String getOidcAuthenticationYaml() {
        boolean isBasic = adm.basicCredentials != null
        if (isBasic) {
            return ""
        } else {
            """
authHttpClientConfiguration:
  clientId: ${adm.oidcCredentials.client_id}
  clientSecret: ${adm.oidcCredentials.client_secret}
  tokenUri: ${adm.host}${adm.oidcCredentials.token_uri}
  timeout: 30 seconds
  ${getTlsConfig()}
"""
        }
    }

    private String getTlsConfig() {
        String file = System.getProperty("javax.net.ssl.trustStore")
        if (file == null) {
            if (getDomainUrl().startsWith("https")) {
                println "No explicit truststore to ADM set, using implicit from JAVA or OS. Specify with javax.net.ssl.keyStoreType system properties."
            }
            return "";
        }

        println ("  Truststore for ADS to ADM set: " + file)
        String password  = System.getProperty("javax.net.ssl.trustStorePassword")
        String type = System.getProperty("javax.net.ssl.trustStoreType", "JKS")
        return """
  tls:
    trustStorePath: ${file}
    trustStorePassword: ${password}
    trustStoreType: ${type}

""";
    }

}
