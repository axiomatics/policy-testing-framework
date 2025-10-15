package tasks

import com.axiomatics.alfa.XacmlDslStandaloneSetup
import extra.AlfaStandaloneJavaGenerator
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.xtext.generator.IGenerator2
import org.eclipse.xtext.generator.JavaIoFileSystemAccess
import org.eclipse.xtext.validation.IResourceValidator
import org.eclipse.xtext.validation.Issue
import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.StopExecutionException
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskExecutionException


import java.util.stream.Collectors

@org.gradle.api.tasks.CacheableTask
class AlfaCompilationTask extends DefaultTask implements Runnable {

    @Input
    boolean extraAttributeIdCheck = true;

    @TaskAction
    void run() {
        try {

            Set<String> collect = inputs.files.toList().stream().map(File::toString).
                    map(s -> "file:/" + s.replace("\\", "/")).collect(Collectors.toSet());
            String outDir = outputs.getFiles().singleFile
            logger.info("Compiling ${collect.size()} ALFA files to ${outDir}: ${collect.stream().collect(Collectors.joining(","))}")

            outputs.previousOutputFiles.forEach(File::delete)

            List<Issue> issues = getLanguage(extraAttributeIdCheck, this.logger).run(collect, outDir)

            if (!issues.isEmpty()) {
                System.err.println("ERROR: " + issues.size() + " Alfa error(s) occurred during build:")
                Iterator var8 = issues.iterator();

                while (var8.hasNext()) {
                    Issue issue = (Issue) var8.next();
                    System.err.println(prettyPrint(issue));
                }
                throw new StopExecutionException("Build failed due to Alfa error(s)")
            }

        } catch (StopExecutionException e) {
            this.didWork = false;
            throw new TaskExecutionException(this, e);
        } catch (Throwable e) {
            e.printStackTrace()
            this.didWork = false;
            throw new TaskExecutionException(this, e)
        }
    }

    private static AlfaStandaloneJavaGenerator getLanguage(boolean extraAttributeIdCheck, Logger logger) {
        def injector = new XacmlDslStandaloneSetup().createInjectorAndDoEMFRegistration();
        IGenerator2 g = injector.getProvider(IGenerator2.class).get()
        IResourceValidator v = injector.getProvider(IResourceValidator.class).get()
        ResourceSet resourceSet = injector.getProvider(ResourceSet.class).get()
        JavaIoFileSystemAccess f = injector.getProvider(JavaIoFileSystemAccess.class).get()
        new AlfaStandaloneJavaGenerator(resourceSet, f, v, g, extraAttributeIdCheck, logger)
    }

    private static String prettyPrint(Issue issue) {
        StringBuilder result = new StringBuilder(issue.getSeverity().name());
        result.append(":").append(issue.getMessage());
        result.append(" (");
        if (issue.getUriToProblem() != null) {
            result.append(issue.getUriToProblem().trimFragment().toString().replace("file:/", "file:///"));
        }
        result.append(":").append(issue.getLineNumber()).append(" col : ").append(issue.getColumn()).append(")");
        return result.toString();
    }
}


