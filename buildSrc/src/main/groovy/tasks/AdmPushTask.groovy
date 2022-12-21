package tasks

import groovy.json.JsonSlurper
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal

import java.util.function.Supplier

class AdmPushTask extends AbstractAdmTask {

    @InputFile
    File domainFile

    @Internal
    String newDomainId

    protected String getMakeBody() {
        com.fasterxml.jackson.dataformat.yaml.YAMLFactory
        File yamlFile = domainFile
        logger.lifecycle("Source file is " + yamlFile.absolutePath);
        def yamlString = yamlFile.text
        com.fasterxml.jackson.databind.ObjectMapper yamlReader =
                new com.fasterxml.jackson.databind.ObjectMapper(
                        new com.fasterxml.jackson.dataformat.yaml.YAMLFactory());
        Object obj = yamlReader.readValue(yamlString, Object.class);
        com.fasterxml.jackson.databind.ObjectMapper jsonWriter = new com.fasterxml.jackson.databind.ObjectMapper();
        jsonWriter.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    }

    @Override
    protected String getRequestMethod() {
        "POST"
    }

    @Override
    protected String getAdmUrlTemplate() {
        adm.apiUrlPushStandaloneTemplate
    }

    @Override
    protected String getAsmAdmUrlTemplate() {
        adm.apiUrlPushAsmTemplate;
    }

    @Override
    protected int getExpectedHttpReturnCode() {
        201
    }

    @Override
    protected void handleResposne(Supplier<String> response) {
        newDomainId = new JsonSlurper().parseText(response.get()).domainId
        logger.lifecycle("Domain successfully pushed to ${getUrl()}. Server returned new id ${newDomainId}")
    }
}