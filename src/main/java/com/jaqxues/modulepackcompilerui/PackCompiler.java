package com.jaqxues.modulepackcompilerui;

import com.jaqxues.modulepackcompilerui.exceptions.CMDException;
import com.jaqxues.modulepackcompilerui.exceptions.NotCompiledException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import javafx.concurrent.Task;
import javafx.util.Callback;

import static com.jaqxues.modulepackcompilerui.PreferenceManager.getPref;
import static com.jaqxues.modulepackcompilerui.PreferencesDef.JDK_INSTALLATION_PATH;
import static com.jaqxues.modulepackcompilerui.PreferencesDef.MODULE_PACKAGE;
import static com.jaqxues.modulepackcompilerui.PreferencesDef.PROJECT_ROOT;
import static com.jaqxues.modulepackcompilerui.PreferencesDef.SDK_BUILD_TOOLS;

/**
 * This file was created by Jacques (jaqxues) in the Project ModulePackCompilerUI.<br>
 * Date: 07.10.2018 - Time 16:29.
 */

public class PackCompiler extends Task<File> {

    private PackOptions packOptions;

    public PackCompiler(PackOptions packOptions) {
        this.packOptions = packOptions;

    }

    public void init(Callback<Double, Double> callback) throws Exception {
        File currentPath = new File("Files");
        File compiledPath = new File(currentPath.getAbsolutePath(), "Compiled");
        File freshCompiledDevJar = new File(currentPath.getAbsolutePath(), "freshCompiledDevJar.jar");
        File preCompiledSToolsJar = new File(currentPath.getAbsolutePath(), "PreCompiledSTools.jar");
        File jarTarget = new File(currentPath.getAbsolutePath(), "\\Packs\\STModulePack");


        // ========================================================================================
        // Copy Class Files
        // ========================================================================================
        try {
            compiledPath.mkdirs();

            copySources(compiledPath, sourceFiles);

            File sourceJava = new File(getPref(PROJECT_ROOT)
                    + (debug ? "/app/build/intermediates/transforms/desugar/pack/debug/0/" : "/app/build/intermediates/transforms/desugar/pack/release/0/")
                    + getPref(MODULE_PACKAGE));


            File sourceKotlin = new File(getPref(PROJECT_ROOT)
                    + (debug ? "/app/build/tmp/kotlin-classes/packDebug/" : "/app/build/tmp/kotlin-classes/packRelease/")
                    + getPref(MODULE_PACKAGE)
            );

            // Set Progress to 10%
            callback.call(0.1d);

            FileUtils.copyFileOrFolder(
                    freshCompiledDevJar,
                    preCompiledSToolsJar,
                    StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING
            );


        } catch (IOException e) {
            LogUtils.getLogger().error("Could not copy Files", e);
            throw e;
        }
        callback.call(0.2d);

        createManifestFile(currentPath);

        jarTarget.getParentFile().mkdirs();
        try {
            String currentCommand = getPref(JDK_INSTALLATION_PATH) + "\\bin\\jar.exe uf " + preCompiledSToolsJar.getAbsolutePath() + " " + compiledPath.getAbsolutePath() + "\\" + getPref(MODULE_PACKAGE);
            cmdProcess(currentCommand);

            currentCommand = getPref(SDK_BUILD_TOOLS) + "\\dx.bat --dex --output=" + jarTarget.getAbsolutePath() + "_unsigned.jar " + compiledPath.getAbsolutePath();
            cmdProcess(currentCommand);

            File manifest = new File(currentPath, "Manifest.txt");
            currentCommand = getPref(JDK_INSTALLATION_PATH) + "\\bin\\jar.exe umf " + manifest.getAbsolutePath() + " " + jarTarget.getAbsolutePath() + "_unsigned.jar";

            cmdProcess(currentCommand);
        } catch (CMDException e) {
            throw new NotCompiledException("Error while executing commands in CMD", e);
        }

        signOutput(new File(jarTarget.getAbsoluteFile() + "_unsigned.jar"));

        adbPush(jarTarget);
    }

    private static void createManifestFile(File currentPath) throws IOException {
        File manifest = new File(currentPath, "Manifest.txt");
        if (!manifest.exists())
            manifest.createNewFile();
        FileWriter writer = new FileWriter(manifest);
        writer.write("Type: Premium\nSCVersion: 10.26.5.0\nPackVersion: 2.0.0.0\nDevelopment: TRUE\nFlavour: prod");
        writer.flush();
        writer.close();
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

    @Override
    protected File call() throws Exception {
        return null;
    }
}
