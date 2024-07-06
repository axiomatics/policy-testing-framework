import com.axiomatics.domtool.cli.ConfigurableDomainCommand
import com.fasterxml.jackson.databind.ObjectMapper
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.*
import org.yaml.snakeyaml.Yaml

import java.nio.charset.StandardCharsets
import java.nio.file.Path

class BuildAuthZDomainTask extends DefaultTask {

    @InputFiles
    File srcDir

    @InputFiles
    FileTree xmlPolicies

    @OutputFile
    File domainFile

    @Input
    String mainPolicy

    @InputFile
    File versionFile


    @TaskAction
    execute() {
        if (!versionFile.canRead()) {
            throw new RuntimeException("Cannot read version file "+ versionFile)
        }
        def command = new ConfigurableDomainCommand();
        command.srcDir = srcDir;
        command.xmlPolicies = xmlPolicies;
        command.mainPolicy = mainPolicy;
        command.outputYamlFile = domainFile

        command.metadataYamlFile = createMetadataFile(srcDir.toPath().resolve("metadata.yaml"),  project.extensions.alfa.metadata)
        command.domainIdentityGitHash = versionFile.getText("UTF-8");
        logger.info("Set version to ${command.domainIdentityGitHash}  based on content in ${versionFile.absolutePath}")
        project.buildDir.toPath().resolve("version").toFile().write(versionFile.getText("UTF-8")[-7..-1],"UTF-8")

        logger.info("Building authorization domain from ${srcDir} with ${xmlPolicies.size()} xacml policies. Main policy" +
                " is ${mainPolicy}. Domain identity: ${ command.domainIdentityGitHash}. Output to ${domainFile.getAbsolutePath()}")
        command.call();
    }

    def Path createMetadataFile(Path srcMetadataYaml, Map<String, String> inProcessMetaDataMap) {
        Map<String,String> metadataMap = new HashMap();
        logger.info("InProcessMetaDataMap contains ${inProcessMetaDataMap.entrySet().size()} entries; " + inProcessMetaDataMap.inspect())
        metadataMap.putAll(inProcessMetaDataMap)

        if (srcMetadataYaml != null && srcMetadataYaml.toFile().canRead()) {
            try {
                logger.info("Reading metadata from " + srcMetadataYaml)
                ObjectMapper objectMapper = new ObjectMapper(new com.fasterxml.jackson.dataformat.yaml.YAMLFactory());
                File metaDataFile = srcMetadataYaml.toFile();

                    try {
                        Map<String, Map<String, String>> srcMetadataMap = objectMapper.readValue(metaDataFile, Map.class);
                        logger.info("Got " + srcMetadataMap.size() + " entries from " + srcMetadataYaml);
                        metadataMap.putAll(srcMetadataMap)
                    } catch (com.fasterxml.jackson.databind.exc.MismatchedInputException e) {
                        logger.info("No entries to map found in " + metaDataFile +". Content " + metaDataFile.text ,e);
                    }


            } catch (Exception e) {
                throw new RuntimeException("Failed to parse metadata file at " + srcMetadataYaml,e);
            }
        } else {
            logger.info(srcMetadataYaml + " is null or not readable.");
        }
        File mapFile = File.createTempFile("alfa-domain-metadata-", ".yaml")
        mapFile.deleteOnExit()
        try {
            Yaml yaml = new Yaml();
            final FileWriter writer = new FileWriter(mapFile, StandardCharsets.UTF_8, false);
            yaml.dump(metadataMap, writer);
            logger.info("Wrote metadatamap to " + mapFile + ": ${mapFile.readLines()} ")
        } catch (Exception e) {
            throw new RuntimeException("Failed to write metadata file to " + mapFile,e);
        }
        return mapFile.toPath()
    }
}

