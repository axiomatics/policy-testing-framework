package extra


import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.xtext.generator.IGenerator2
import org.eclipse.xtext.generator.IGeneratorContext
import org.eclipse.xtext.generator.JavaIoFileSystemAccess
import org.eclipse.xtext.util.CancelIndicator
import org.eclipse.xtext.validation.CheckMode
import org.eclipse.xtext.validation.IResourceValidator
import org.eclipse.xtext.validation.Issue
import org.gradle.api.logging.Logger


class AlfaStandaloneJavaGenerator {

    private ResourceSet resourceSet
    private IResourceValidator validator;
    private IGenerator2 generator;
    private JavaIoFileSystemAccess fileAccess;
    private boolean extraAttributeIdCheck;
    private Logger logger

    public AlfaStandaloneJavaGenerator(ResourceSet resourceSet, JavaIoFileSystemAccess fileAccess, IResourceValidator validator, IGenerator2 generator, boolean extraAttributeIdCheck, Logger logger) {
        this.resourceSet = resourceSet;
        this.fileAccess = fileAccess;
        this.validator = validator
        this.generator = generator;
        this.extraAttributeIdCheck = extraAttributeIdCheck
        this.logger = logger;
    }

     public List<Issue>  run(Set<String> sources, String outputFolderName) {

        Set<Resource> resources = new LinkedHashSet();
        Iterator var5 = sources.iterator();

        while(var5.hasNext()) {
            String fileName = (String)var5.next();
            Resource modelResource = resourceSet.getResource(URI.createURI(fileName), true);
            resources.add(modelResource);
        }

        fileAccess.setOutputPath(outputFolderName);
        var5 = resources.iterator();

        Resource resource;
        while(var5.hasNext()) {
            resource = (Resource)var5.next();
            if (extraAttributeIdCheck) {
                logger.debug("Calling extra attribute validation before compilation")
                ExtraAlfaValidator.validate(resource);
            }
            List<Issue> list = validator.validate(resource, CheckMode.ALL, CancelIndicator.NullImpl);
            if (!list.isEmpty()) {
              return list
            }
        }

        var5 = resources.iterator();

        while(var5.hasNext()) {
            resource = (Resource)var5.next();
            generator.doGenerate(resource, fileAccess, (IGeneratorContext)null);
        }

        return Collections.emptyList();
    }
}
