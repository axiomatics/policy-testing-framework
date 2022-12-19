import com.axiomatics.domtool.cli.ConfigurableDomainCommand
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

class BuildAuthZDomainTask extends DefaultTask {

    @InputFiles
    FileTree srcDir

    @InputFiles
    FileTree xmlPolicies

    @OutputFile
    File domainFile

    @Input
    String mainPolicy

    @TaskAction
    execute() {
        def command = new ConfigurableDomainCommand();
        command.srcDir = srcDir;
        command.xmlPolicies = xmlPolicies;
        command.mainPolicy = mainPolicy;
        command.outputYamlFile = domainFile
        logger.info("Building authorization domain from ${srcDir} with ${xmlPolicies.size()} xacml policies. Main policy" +
                " is ${mainPolicy}. Output to ${domainFile.getAbsolutePath()}")
        command.call();
    }

}

