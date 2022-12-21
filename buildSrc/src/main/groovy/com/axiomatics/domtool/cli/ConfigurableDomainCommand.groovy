package com.axiomatics.domtool.cli

import org.gradle.api.file.FileTree

public class ConfigurableDomainCommand extends com.axiomatics.domtool.cli.DomainCommand {

    // NOTE! Do not move this class to another pacakge, it needs to access package private methods

    private static final String MAIN_POLICY_PREFIX = "http://axiomatics.com/alfa/identifier/"
    public FileTree srcDir;
    public FileTree xmlPolicies;
    public String mainPolicy;
    public File outputYamlFile

    public Integer call() {

        super.inputPath =   srcDir.getDir().toPath()
        super.mainPolicyIdentifierOverride =  MAIN_POLICY_PREFIX + mainPolicy
        super.xacmlSpecificationsPath = xmlPolicies.getDir().toPath()
        super.outputPath = outputYamlFile.toPath()
        super.call();
    }
}
