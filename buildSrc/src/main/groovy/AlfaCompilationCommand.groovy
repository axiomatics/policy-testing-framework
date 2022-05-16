import com.axiomatics.alfa.XacmlDslStandaloneSetup
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.xtext.generator.IGenerator2
import org.eclipse.xtext.generator.JavaIoFileSystemAccess
import org.eclipse.xtext.validation.IResourceValidator
import org.eclipse.xtext.validation.Issue
import org.gradle.api.tasks.StopExecutionException
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskExecutionException

import java.util.stream.Collectors

class AlfaCompilationCommand extends org.gradle.api.DefaultTask implements Runnable {

    @TaskAction
    void run() {
        try {

            Set<String> collect = inputs.files.toList().stream().map(File::toString).
                    map(s -> "file:/" + s.replace("\\", "/")).collect(Collectors.toSet());
            String out = outputs.getFiles().singleFile

            outputs.previousOutputFiles.forEach(File::delete)

            List<Issue> issues = getLanguage().run(collect, out)

            if (!issues.isEmpty()) {
                System.err.println("ERROR:" + issues.size() + " Alfa errors")
                Iterator var8 = issues.iterator();

                while (var8.hasNext()) {
                    Issue issue = (Issue) var8.next();
                    System.err.println(prettyPrint(issue));
                }
                throw new StopExecutionException(issues.size() + " Alfa errors")
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

    private static AlfaStandaloneJavaGenerator getLanguage() {
        def injector = new XacmlDslStandaloneSetup().createInjectorAndDoEMFRegistration();
        IGenerator2 g = injector.getProvider(IGenerator2.class).get()
        IResourceValidator v = injector.getProvider(IResourceValidator.class).get()
        ResourceSet resourceSet = injector.getProvider(ResourceSet.class).get()
        JavaIoFileSystemAccess f = injector.getProvider(JavaIoFileSystemAccess.class).get()
        new AlfaStandaloneJavaGenerator(resourceSet, f, v, g)
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


