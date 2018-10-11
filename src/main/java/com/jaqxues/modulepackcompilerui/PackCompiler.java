package com.jaqxues.modulepackcompilerui;

import com.jaqxues.modulepackcompilerui.exceptions.CMDException;
import com.jaqxues.modulepackcompilerui.exceptions.NotCompiledException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import javafx.concurrent.Task;

import static com.jaqxues.modulepackcompilerui.PreferenceManager.getPref;
import static com.jaqxues.modulepackcompilerui.PreferencesDef.JDK_INSTALLATION_PATH;
import static com.jaqxues.modulepackcompilerui.PreferencesDef.MODULE_PACKAGE;
import static com.jaqxues.modulepackcompilerui.PreferencesDef.SDK_BUILD_TOOLS;

/**
 * This file was created by Jacques (jaqxues) in the Project ModulePackCompilerUI.<br>
 * Date: 07.10.2018 - Time 16:29.
 */

public class PackCompiler extends Task<File> {
    private File[] sources;
    private String[] attributes;
    private File jarTarget;
    private SignConfig signConfig;
    private File currentPath = new File("Files");
    private File compiledPath = new File(currentPath.getAbsolutePath(), "Compiled");
    private File freshCompiledDevJar = new File(currentPath.getAbsolutePath(), "freshCompiledDevJar.jar");
    private File preCompiledSToolsJar = new File(currentPath.getAbsolutePath(), "PreCompiledSTools.jar");

    private PackCompiler(File[] sources, String[] attributes, File jarTarget, SignConfig signConfig) {
        this.sources = sources;
        this.attributes = attributes;
        this.jarTarget = jarTarget;
        this.signConfig = signConfig;
    }

    private static void copySources(File dest, File... sources) throws IOException, NotCompiledException {
        // Delete Previous Files
        Files.walkFileTree(dest.toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });


        for (File source : sources) {
            if (!source.exists())
                throw new NotCompiledException("Class Files not found, please check if you compiled the Pack. If you already compiled it, check the Module Package Name and the Project Root\n\nSource Folder: " + source.getAbsolutePath());

        }

        File targetFile = new File(dest, getPref(MODULE_PACKAGE));
        targetFile.mkdirs();

        for (File source : sources) {
            FileUtils.copyFileOrFolder(
                    source,
                    targetFile,
                    StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING
            );
        }
    }

    private static void cmdProcess(String cmd) throws CMDException {
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            if (process.waitFor() != 0)
                throw new CMDException("Could not execute command \"" + cmd + "\"");

        } catch (InterruptedException | IOException e) {
            throw new CMDException("Unable to execute command \"" + cmd + "\"");
        }
    }

    private static void signOutput(File signInput) throws Exception {
        String command = "\"" + getPref(JDK_INSTALLATION_PATH) + "\\bin\\jarsigner.exe\" -tsa https://timestamp.digicert.com -keystore D:\\Documents\\Jacques\\CodeProjects\\IdeaProjects\\SnapTools\\jaqxues\\SnapTools\\KeysCertificates\\KeyStore\\KeyStore.jks -signedjar D:\\Documents\\Jacques\\CodeProjects\\IdeaProjects\\SnapTools\\jaqxues\\ModulePackCompilerUI\\Files\\Packs\\as.jar " + signInput.getAbsolutePath() + " Master";
        System.out.println(command);
        Process process = Runtime.getRuntime().exec(command);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(process.getOutputStream());
        outputStreamWriter.append("");
        outputStreamWriter.flush();
        outputStreamWriter.close();
    }

    private static void adbPush(File file) throws Exception {
        cmdProcess("adb push " + file.getAbsolutePath() + ".jar" + "/sdcard/SnapTools/ModulePacks");
    }

    public void init() throws Exception {

        // ========================================================================================
        // Copy Class Files
        // ========================================================================================
        try {
            compiledPath.mkdirs();

            copySources(compiledPath, sources);

            FileUtils.copyFileOrFolder(
                    freshCompiledDevJar,
                    preCompiledSToolsJar,
                    StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING
            );


        } catch (IOException e) {
            LogUtils.getLogger().error("Could not copy Files", e);
            throw e;
        }

        File manifest = createManifestFile();

        jarTarget.getParentFile().mkdirs();

        String[] commands = getCommands(manifest);
        try {
            for (String string : commands)
                cmdProcess(string);
        } catch (CMDException e) {
            throw new NotCompiledException("Error while executing commands in CMD", e);
        }

        signOutput(jarTarget);

        adbPush(jarTarget);
    }

    private String[] getCommands(File manifest) {
        return new String[]{
                getPref(JDK_INSTALLATION_PATH) + "\\bin\\jar.exe uf " + preCompiledSToolsJar.getAbsolutePath() + " " + compiledPath.getAbsolutePath() + "\\" + getPref(MODULE_PACKAGE),
                getPref(SDK_BUILD_TOOLS) + "\\dx.bat --dex --output=" + jarTarget.getAbsolutePath() + "_unsigned.jar " + compiledPath.getAbsolutePath(),
                getPref(JDK_INSTALLATION_PATH) + "\\bin\\jar.exe umf " + manifest.getAbsolutePath() + " " + jarTarget.getAbsolutePath() + "_unsigned.jar"
        };
    }

    private File createManifestFile() throws IOException {
        File manifest = new File(currentPath, "Manifest.txt");
        if (!manifest.exists())
            manifest.createNewFile();
        FileWriter writer = new FileWriter(manifest);
        writer.write("Type: Premium\nSCVersion: 10.26.5.0\nPackVersion: 2.0.0.0\nDevelopment: TRUE\nFlavour: prod");
        writer.flush();
        writer.close();
        return manifest;
    }

    @Override
    protected File call() throws Exception {
        init(); // TODO Elegance
        return null;
    }

    public static class Builder {
        private List<File> sources;
        private List<String> attributes;
        private File jarTarget;
        private SignConfig signConfig;

        public List<File> getSources() {
            return sources;
        }

        public Builder setSources(List<File> sources) {
            this.sources = sources;
            return this;
        }

        public List<String> getAttributes() {
            return attributes;
        }

        public Builder setAttributes(List<String> attributes) {
            this.attributes = attributes;
            return this;
        }

        public File getJarTarget() {
            return jarTarget;
        }

        public Builder setJarTarget(File jarTarget) {
            this.jarTarget = jarTarget;
            return this;
        }

        public SignConfig getSignConfig() {
            return signConfig;
        }

        public Builder setSignConfig(SignConfig signConfig) {
            this.signConfig = signConfig;
            return this;
        }

        public PackCompiler build() throws IllegalArgumentException {
            if (sources == null || sources.size() == 0)
                throw new IllegalArgumentException("No Source Files specified");
            if (attributes == null) {
                LogUtils.getLogger().warn("No Attributes provided");
                attributes = new ArrayList<>();
            }
            if (jarTarget == null) {
                throw new IllegalArgumentException("No Jar Target Provided");
            }
            return new PackCompiler((File[]) sources.toArray(), (String[]) attributes.toArray(), jarTarget, signConfig);
        }
    }
}
