package com.jaqxues.modulepackcompilerui;

import java.io.File;
import java.util.List;

import javax.annotation.Nullable;

/**
 * This file was created by Jacques (jaqxues) in the Project ModulePackCompilerUI.<br>
 * Date: 10.10.2018 - Time 20:28.
 */

public class PackOptions {
    private List<String> attributes;
    private File[] sourceFiles;
    private File targetJar;
    private File currentPath;
    private SignConfig signConfig;

    public PackOptions(List<String> attributes, File[] sourceFiles, File targetJar, File currentPath, @Nullable SignConfig signConfig) {
        this.attributes = attributes;
        this.sourceFiles = sourceFiles;
        this.targetJar = targetJar;
        this.currentPath = currentPath;
        this.signConfig = signConfig;
    }

    public List<String> getAttributes() {
        return attributes;
    }

    public File[] getSourceFiles() {
        return sourceFiles;
    }

    public File getTargetJar() {
        return targetJar;
    }

    public File getCurrentPath() {
        return currentPath;
    }

    public SignConfig getSignConfig() {
        return signConfig;
    }
}
