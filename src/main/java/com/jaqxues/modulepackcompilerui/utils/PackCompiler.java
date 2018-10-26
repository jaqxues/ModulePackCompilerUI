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
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.RemoteFile;

import static com.jaqxues.modulepackcompilerui.preferences.PreferenceManager.getPref;
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
    private VirtualAdbDeviceModel[] jadbDevices;
    private File jarTarget;
    private SignConfigModel signConfig;
    private File currentPath = new File("Files/Process");
    private File compiledPath = new File(currentPath.getAbsolutePath(), "Compiled");
    private File preCompiledSToolsJar = new File(currentPath.getAbsolutePath(), "PreCompiledSTools.jar");

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
     * @param file The file that should be pushed
     * @param vAdbDevs The devices that the file should be pushed to
     * @return A bundled List of all Exceptions that occurred as the program should still try to copy the Files to the other devices.
     */
    private static List<Exception> adbPush(File file, VirtualAdbDeviceModel[] vAdbDevs) {
        List<Exception> exceptions = new ArrayList<>();
        for (VirtualAdbDeviceModel vAdbDev : vAdbDevs) {
            if (vAdbDev.isConnected()) {
                JadbDevice jadbDevice = vAdbDev.getDevice();
                try {
                    jadbDevice.push(file, new RemoteFile(vAdbDev.getPushPath() + file.getName()));
                } catch (Exception e) {
                    exceptions.add(e);
                }
            }
        }
        return exceptions;
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

    public void init() throws Exception {

        // ========================================================================================
        // Copy Class Files
        // ========================================================================================
        try {
            compiledPath.mkdirs();
            copySources(compiledPath, sources);

            FileUtils.copyFile(
                    getClass().getResource("/freshCompiledDevJar.jar").openStream(),
                    preCompiledSToolsJar,
                    StandardCopyOption.REPLACE_EXISTING
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

        if (getPref(ADB_PUSH_TOGGLE)) {
            List<Exception> exceptions = adbPush(jarTarget, jadbDevices);
            if (exceptions.size() > 0) {
                LogUtils.getLogger().error("One or more errors while pushing the Files to the Adb Device.", Arrays.deepToString(exceptions.toArray()));
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Adb Push");
                alert.setHeaderText("Error while pushing the File to an ADB Device");
                alert.setContentText("One or more Errors while pushing the files to the device.");
                alert.show();
            }
        }
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
            return new PackCompiler(sources.toArray(new File[0]), attributes.toArray(new String[0]), vAdbDevices.toArray(new VirtualAdbDeviceModel[0]), jarTarget, signConfig);
        }
    }
}