package com.jaqxues.modulepackcompilerui.utils;

import com.jaqxues.modulepackcompilerui.exceptions.CMDException;
import com.jaqxues.modulepackcompilerui.exceptions.NotCompiledException;
import com.jaqxues.modulepackcompilerui.models.SignConfigModel;
import com.jaqxues.modulepackcompilerui.models.VirtualAdbDeviceModel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.scene.control.Alert;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.RemoteFile;

import static com.jaqxues.modulepackcompilerui.preferences.PreferenceManager.getPref;
import static com.jaqxues.modulepackcompilerui.preferences.PreferencesDef.*;

/**
 * This file was created by Jacques (jaqxues) in the Project ModulePackCompilerUI.<br>
 * Date: 07.10.2018 - Time 16:29.
 */

public class PackCompiler {
    private static final AtomicBoolean compiling = new AtomicBoolean();
    private final File[] sources;
    private final String[] attributes;
    private final VirtualAdbDeviceModel[] jadbDevices;
    private final File jarTarget;
    private boolean signed = false;
    private final SignConfigModel signConfig;
    private final File currentPath = new File("Files/Process");
    private final File compiledPath = new File(currentPath.getAbsolutePath(), "Compiled");
    private final File preCompiledSToolsJar = new File(currentPath.getAbsolutePath(), "PreCompiledSTools.jar");

    private PackCompiler(File[] sources, String[] attributes, VirtualAdbDeviceModel[] jadbDevices, File jarTarget, SignConfigModel signConfig) {
        this.sources = sources;
        this.attributes = attributes;
        this.jadbDevices = jadbDevices;
        this.jarTarget = jarTarget;
        this.signConfig = signConfig;
    }

    /**
     * Copy Files in <code>sources</code> to the <code>dest</code> Folder. This clears the
     * <code>dest</code> Folder, then copies every single file from the <code>sources</code>
     * recursively into the Destination Folder.
     *
     * @param dest    The Destination Path in form of a File
     * @param sources The Source File Directories containing the files you want to copy
     * @throws IOException          Java Exception while copying and deleting files
     * @throws NotCompiledException In case a Source in <code>sources</code> has not been found
     */
    private static void copySources(@Nonnull File dest, File... sources) throws IOException, NotCompiledException {
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
     * Handles pushing the ModulePack via ADB to the Adb Device (using Jadb)
     *
     * @param file     The file that should be pushed
     * @param vAdbDevs The devices that the file should be pushed to
     * @return A bundled List of all Exceptions that occurred as the program should still try to copy the Files to the other devices.
     */
    private List<Exception> adbPush(File file, VirtualAdbDeviceModel[] vAdbDevs) {
        List<Exception> exceptions = new ArrayList<>();
        for (VirtualAdbDeviceModel vAdbDev : vAdbDevs) {
            if (vAdbDev.isConnected()) {
                JadbDevice jadbDevice = vAdbDev.getDevice();
                String path = Paths.get(vAdbDev.getPushPath(), file.getName() + (signed ? ".jar" : "_unsigned.jar")).toString()
                        // Android does not play well with "\" in paths
                        .replace('\\', '/');
                try {
                    jadbDevice.push(new File(file.getAbsolutePath() + (signed ? ".jar" : "_unsigned.jar")), new RemoteFile(path));
                } catch (Exception e) {
                    exceptions.add(new Exception("Could not push file to path " + path, e));
                }
            }
        }
        return exceptions;
    }

    public static String getSTFileNameFromTemplate(List<String> attributes) {
        String type = null;
        String scVersion = null;
        String flavour = null;
        String packVersion = null;
        for (String attribute : attributes) {
            switch (attribute.split("=")[0]) {
                case "Flavour":
                    flavour = attribute.split("=", 2)[1].trim();
                    break;
                case "Type":
                    type = attribute.split("=", 2)[1].trim();
                    break;
                case "SCVersion":
                    scVersion = attribute.split("=", 2)[1].trim();
                    break;
                case "PackVersion":
                    packVersion = attribute.split("=", 2)[1].trim();
                    break;
            }
        }
        if (type == null || scVersion == null || flavour == null)
            return "";
        return getSTFileNameFromTemplate(type, scVersion, flavour, packVersion).replace(" ", "_");
    }

    /**
     * Copy from the SnapTools Project
     * ===========================================================================
     * A utility method to generalise filename generation instead of having
     * duplicate code throughout the project.
     * ===========================================================================
     *
     * @return A string that can be used to determine the filename of a ModulePack
     * based on some MetaData
     */
    public static String getSTFileNameFromTemplate(String type, String scVersion, String flavour, @Nullable String packVersion) {
        return "STModulePack"
                + (!"prod".equals(flavour) ? getFlavourText(flavour) : "")
                + (packVersion == null ? "" : "_" + packVersion)
                + "_" + type
                + "_" + scVersion;
    }

    public static String getFlavourText(String flavour) {
        if (flavour != null) {
            switch (flavour.toLowerCase()) {
                case "beta":
                    return "Beta";
                case "prod":
                    return "Release";
            }
        }
        LogUtils.getLogger().error("Unable to get FlavourText", new Exception("Unknown Flavour: " + flavour));
        return flavour;
    }

    /**
     * Signs the {@link PackCompiler#jarTarget}_unsigned.jar, outputs {@link PackCompiler#jarTarget}.jar
     *
     * @throws Exception Runtime Exception while running command and Signing the Jar
     */
    private void signOutput() throws Exception {
        String checkedKey = MiscUtils.checkSignKey(signConfig);
        if (checkedKey != null)
            throw new NotCompiledException(checkedKey);
        String jarsignerExe = MiscUtils.executableCMD(getPref(JDK_INSTALLATION_PATH) ,"/bin/jarsigner", "exe");
        String command = String.format("%s -tsa http://timestamp.digicert.com -keystore %s -signedjar %s.jar %s_unsigned.jar %s",
                jarsignerExe,
                signConfig.getStorePath(),
                jarTarget.getAbsolutePath(),
                jarTarget.getAbsolutePath(),
                signConfig.getKeyAlias()
        );
        LogUtils.getLogger().debug("Generated Command: " + command);
        Process process = Runtime.getRuntime().exec(command);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(process.getOutputStream());
        outputStreamWriter.append(signConfig.getStorePassword())
                .append("\n")
                .append(signConfig.getKeyPassword())
                .flush();
        outputStreamWriter.close();
        if (process.waitFor() != 0)
            throw new CMDException("Could not execute Signing");
        signed = true;
    }

    public boolean init() throws Exception {
        if (compiling.getAndSet(true)) {
            MiscUtils.showAlert(
                    Alert.AlertType.INFORMATION,
                    "Module Pack Compiler",
                    "Already compiling on another thread",
                    "Unfortunately it is not possible to compile different Packs at the same time."
            );
            return false;
        }

        // ========================================================================================
        // Copy Class Files
        // ========================================================================================
        try {
            compiledPath.mkdirs();
            copySources(compiledPath, sources);

            FileUtils.copyFile(
                    // Since shadowJar cannot distinguish between .jar "assets" in /resources and dependencies, we need to shadow it with another file extension
                    getClass().getResourceAsStream("/freshCompiledDevJar.jar.file"),
                    preCompiledSToolsJar,
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException e) {
            LogUtils.getLogger().error("Could not copy Files", e);
            compiling.set(false);
            throw e;
        }

        File manifest = createManifestFile();

        jarTarget.getParentFile().mkdirs();

        String[] commands = getCommands(manifest);
        try {
            for (String string : commands)
                cmdProcess(string);
        } catch (CMDException e) {
            compiling.set(false);
            throw new NotCompiledException("Error while executing commands in CMD", e);
        }

        if ((boolean) getPref(SIGN_PACK) && signConfig != null)
            signOutput();

        if (getPref(ADB_PUSH_TOGGLE)) {
            List<Exception> exceptions = adbPush(jarTarget, jadbDevices);
            if (exceptions.size() > 0) {
                for (Exception ex : exceptions) {
                    LogUtils.getLogger().error("One or more errors while pushing the Files to the Adb Device.", ex);
                }
                MiscUtils.showAlert(
                        Alert.AlertType.ERROR,
                        "Adb Push",
                        "Error while pushing the File to an ADB Device",
                        "One or more Errors while pushing the files to the device."
                );
            }
        }
        compiling.set(false);
        return true;
    }

    public static void setCompiling(boolean compiling) {
        PackCompiler.compiling.set(compiling);
    }

    private static Set<Path> getClassNames(Set<Path> names, File path) {
        for (File file : Objects.requireNonNull(path.listFiles())) {
            if (file.isFile()) {
                names.add(file.toPath());
            } else if (file.isDirectory()) {
                getClassNames(names, file);
            }
        }
        return names;
    }

    /**
     * A simple command to bundle the commands for compiling a ModulePack. We need the JDK Path and
     * the Android SDK Build-Tools Path as we cannot just copy one "jar.exe" file into our resources:
     * It needs more context. It makes sense to just ask for those two paths
     *
     * @param manifest The manifest file for the new Jar
     * @return An Array containing the Commands
     */
    private String[] getCommands(File manifest) throws Exception {
        String jarExe = MiscUtils.executableCMD(getPref(JDK_INSTALLATION_PATH), "/bin/jar", "exe");
        String[] commands = new String[]{
                String.format("%s cfm %s_unsigned.jar %s -C %s classes.dex", jarExe, jarTarget.getAbsolutePath(), manifest.getAbsolutePath(), currentPath.getAbsolutePath())
        };

        new D8Wrapper(new File((String) getPref(SDK_BUILD_TOOLS), "lib/d8.jar"))
                .addProgramFiles(getClassNames(new HashSet<>(), compiledPath))
                .setDexOutput(currentPath.toPath())
                .runCommand();

        for (int i = 0; i < commands.length; i++) {
            // Windows Stuff - Pretty Printing
            if (!File.separator.equals("/")) {
                commands[i] = commands[i].replace("/", File.separator);
            }
        }
        return commands;
    }

    private File createManifestFile() throws IOException {
        File manifest = new File(currentPath, "Manifest.txt");
        if (!manifest.exists())
            manifest.createNewFile();
        FileWriter writer = new FileWriter(manifest);
        StringBuilder builder = new StringBuilder();
        for (String string : attributes) {
            builder.append(string.replace("=", ": "))
                    .append("\n");
        }
        writer.write(builder.toString());
        writer.flush();
        writer.close();
        return manifest;
    }

    public static class Builder {
        private List<File> sources;
        private List<String> attributes;
        private File jarTarget;
        private SignConfigModel signConfig;
        private List<VirtualAdbDeviceModel> vAdbDevices;

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

        public SignConfigModel getSignConfig() {
            return signConfig;
        }

        public Builder setSignConfig(SignConfigModel signConfig) {
            this.signConfig = signConfig;
            return this;
        }

        public List<VirtualAdbDeviceModel> getVirtualAdbDevices() {
            return vAdbDevices;
        }

        public Builder setVirtualAdbDevices(List<VirtualAdbDeviceModel> vAdbDevices) {
            this.vAdbDevices = vAdbDevices;
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
            return new PackCompiler(sources.toArray(new File[0]), attributes.toArray(new String[0]), (vAdbDevices == null) ? new VirtualAdbDeviceModel[0] : vAdbDevices.toArray(new VirtualAdbDeviceModel[0]), jarTarget, signConfig);
        }
    }

    public String getDescription() {
        File projectRoot = new File((String) getPref(PROJECT_ROOT));
        StringBuilder sourcesString = new StringBuilder();
        for (File source : sources) {
            sourcesString
                    .append("\n\t\t")
                    .append(projectRoot.toPath().relativize(source.toPath()).normalize().toString());
        }
        StringBuilder attributesString = new StringBuilder();
        for (String attribute : attributes)
            attributesString.append("\n\t\t").append(attribute.replace("=", ": "));
        StringBuilder jadbDevString = new StringBuilder();
        for (VirtualAdbDeviceModel dev : jadbDevices) {
            jadbDevString.append("\n\t\t").append(dev.getDescription());
        }
        if (jadbDevString.length() == 0)
            jadbDevString.append("None");
        return String.format("Module Pack: %s\n\tProject Root: %s\n\tSources: %s\n\tAdb Devices: %s\n\tPack Directory: %s\n\tSigning Configuration: %s\n\tAttributes: %s",
                jarTarget.getName() + (signed ? ".jar" : "_unsigned.jar"),
                getPref(PROJECT_ROOT),
                sourcesString.toString(),
                jadbDevString.toString(),
                jarTarget.getParentFile().getAbsolutePath(),
                signConfig == null ? "None" : ("\n" + signConfig.getString()).replaceAll("\n", "\n\t\t"),
                attributesString.toString()
        );
    }
}
