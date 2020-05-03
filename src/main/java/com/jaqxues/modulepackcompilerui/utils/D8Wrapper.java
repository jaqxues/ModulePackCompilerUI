package com.jaqxues.modulepackcompilerui.utils;

import com.jaqxues.modulepackcompilerui.exceptions.D8WrapperException;
import com.sun.webkit.plugin.PluginManager;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Collection;

/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project ModulePackCompilerUI.<br>
 * Date: 5/3/2020 - Time 5:26 PM.
 */
public class D8Wrapper {

    private static final String D8_OUTPUT_MODE_CLASS = "com.android.tools.r8.OutputMode";
    private static final String D8_COMMAND_CLASS = "com.android.tools.r8.D8Command";
    private static final String D8_CLASS = "com.android.tools.r8.D8";

    private ClassLoader classLoader;
    private Object d8Command;

    public D8Wrapper(File d8JarFile) throws D8WrapperException {
            try {
                System.out.println(d8JarFile.toURI().toURL());
                classLoader = new URLClassLoader(
                        new URL[]{d8JarFile.toURI().toURL()},
                        PluginManager.class.getClassLoader()
                );
                d8Command = classLoader.loadClass(D8_COMMAND_CLASS).getMethod("builder").invoke(null);
            } catch (Exception e) {
                throw new D8WrapperException(e);
            }
    }

    public D8Wrapper setDexOutput(Path path) throws D8WrapperException {
        try {
            Class outputModeClass = classLoader.loadClass(D8_OUTPUT_MODE_CLASS);
            Object constant = outputModeClass.getEnumConstants()[0];
            Method m = d8Command.getClass().getMethod("setOutput", Path.class, outputModeClass);
            m.invoke(d8Command, path, constant);
            return this;
        } catch (Exception e) {
            throw new D8WrapperException(e);
        }
    }

    public D8Wrapper addProgramFiles(Collection<Path> paths) throws D8WrapperException {
        try {
            d8Command.getClass().getMethod("addProgramFiles", Collection.class)
                    .invoke(d8Command, paths);
            return this;
        } catch (Exception e) {
            throw new D8WrapperException(e);
        }
    }

    public void runCommand() throws D8WrapperException {
        try {
            Object result = d8Command.getClass().getMethod("build")
                    .invoke(d8Command);
            classLoader.loadClass(D8_CLASS)
                    .getMethod("run", result.getClass())
                    .invoke(null, result);
        } catch (Exception e) {
            throw new D8WrapperException(e);
        }
    }
}
