package com.jaqxues.modulepackcompilerui.utils;

import com.jaqxues.modulepackcompilerui.models.SignConfig;
import com.jaqxues.modulepackcompilerui.exceptions.CMDException;
import com.jaqxues.modulepackcompilerui.exceptions.NotCompiledException;
import com.sun.istack.internal.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
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
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;

import static com.jaqxues.modulepackcompilerui.preferences.PreferenceManager.getPref;
import static com.jaqxues.modulepackcompilerui.preferences.PreferencesDef.ADB_PUSH_PATH;
import static com.jaqxues.modulepackcompilerui.preferences.PreferencesDef.ADB_PUSH_TOGGLE;
import static com.jaqxues.modulepackcompilerui.preferences.PreferencesDef.JDK_INSTALLATION_PATH;
import static com.jaqxues.modulepackcompilerui.preferences.PreferencesDef.SDK_BUILD_TOOLS;

/**
 * This file was created by Jacques (jaqxues) in the Project ModulePackCompilerUI.<br>
 * Date: 07.10.2018 - Time 16:29.
 */

public class PackCompiler extends Task<File> {
    private File[] sources;
    private String[] attributes;
    private File jarTarget;
    private SignConfig signConfig;
    private File currentPath = new File("Files/Process");
    private File compiledPath = new File(currentPath.getAbsolutePath(), "Compiled");
    private File freshCompiledDevJar = new File(currentPath.getAbsolutePath(), "freshCompiledDevJar.jar");
    private File preCompiledSToolsJar = new File(currentPath.getAbsolutePath(), "PreCompiledSTools.jar");

    private PackCompiler(File[] sources, String[] attributes, File jarTarget, SignConfig signConfig) {
        this.sources = sources;
        this.attributes = attributes;
        this.jarTarget = jarTarget;
        this.signConfig = signConfig;
    }

    /**
     * Copy Files in <code>sources</code> to the <code>dest</code> Folder. This clears the
     * <code>dest</code> Folder, then copies every single file from the <code>sources</code>
     * recursively into the Destination Folder.
     *
     * @param dest The Destination Path in form of a File
     * @param sources The Source File Directories containing the files you want to copy
     * @throws IOException Java Exception while copying and deleting files
     * @throws NotCompiledException In case a Source in <code>sources</code> has not been found
     */
    private static void copySources(@NotNull File dest, @NotNull File... sources) throws IOException, NotCompiledException {
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

        File targetFile = new File(dest, MiscUtils.getMPFolder());
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

    /**
     * Signs the {@link PackCompiler#jarTarget}_unsigned.jar, outputs {@link PackCompiler#jarTarget}.jar
     * @throws Exception Runtime Exception while running command and Signing the Jar
     */
    private void signOutput() throws Exception {
        String command = String.format("\"%s\\bin\\jarsigner.exe\" -tsa https://timestamp.digicert.com -keystore %s -signedjar %s.jar %s_unsigned.jar %s",
                getPref(JDK_INSTALLATION_PATH),
                signConfig.getStorePath(),
                jarTarget.getAbsolutePath(),
                jarTarget.getAbsolutePath(),
                signConfig.getKeyAlias()
                );
        LogUtils.getLogger().debug("Generated Command: %s", command);
        Process process = Runtime.getRuntime().exec(command);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(process.getOutputStream());
        outputStreamWriter.append(signConfig.getStorePassword())
                .append(signConfig.getKeyPassword())
                .flush();
        outputStreamWriter.close();
        if (process.waitFor() != 0)
            throw new CMDException("Could not execute Signing");
    }

    private static void adbPush(File file) throws Exception {
        if (getPref(ADB_PUSH_TOGGLE)) {
//            cmdProcess("adb");
//            JadbConnection jadb = new JadbConnection();
//            List<JadbDevice> devices = jadb.getDevices();
            // TODO JADB Implementation
            cmdProcess("adb push " + file.getAbsolutePath() + ".jar " + getPref(ADB_PUSH_PATH) + file.getName() + ".jar");
        }
    }

    public void init() throws Exception {

        if (!freshCompiledDevJar.exists())
            throw new FileNotFoundException("You need to place the \"freshCompiledDev.jar\" into \"Files/Process\"");
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

        if (signConfig != null)
            signOutput();

        adbPush(jarTarget);
    }

    private String[] getCommands(File manifest) {
        return new String[]{
                getPref(JDK_INSTALLATION_PATH) + "\\bin\\jar.exe uf " + preCompiledSToolsJar.getAbsolutePath() + " " + compiledPath.getAbsolutePath() + "\\" + MiscUtils.getMPFolder(),
                getPref(SDK_BUILD_TOOLS) + "\\dx.bat --dex --output=" + jarTarget.getAbsolutePath() + "_unsigned.jar " + compiledPath.getAbsolutePath(),
                getPref(JDK_INSTALLATION_PATH) + "\\bin\\jar.exe umf " + manifest.getAbsolutePath() + " " + jarTarget.getAbsolutePath() + "_unsigned.jar"
        };
    }

    private File createManifestFile() throws IOException {
        File manifest = new File(currentPath, "Manifest.txt");
        if (!manifest.exists())
            manifest.createNewFile();
        FileWriter writer = new FileWriter(manifest);
        StringBuilder builder = new StringBuilder();
        String prefix = "";
        for (String string : attributes) {
            builder.append(prefix)
                    .append(string.replace("=", ": "));
            prefix = "\n";
        }
        writer.write(builder.toString());
        writer.flush();
        writer.close();
        return manifest;
    }

    @Override
    public File call() throws Exception {
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
            return new PackCompiler(sources.toArray(new File[0]), attributes.toArray(new String[0]), jarTarget, signConfig);
        }
    }
}
