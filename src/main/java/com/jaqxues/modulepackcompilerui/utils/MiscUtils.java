package com.jaqxues.modulepackcompilerui.utils;

import static com.jaqxues.modulepackcompilerui.preferences.PreferenceManager.getPref;
import static com.jaqxues.modulepackcompilerui.preferences.PreferencesDef.MODULE_PACKAGE;

/**
 * This file was created by Jacques (jaqxues) in the Project ModulePackCompilerUI.<br>
 * Date: 18.10.2018 - Time 15:32.
 */

public class MiscUtils {

    /**
     * Used instead of having duplicates throughout the project for dx.bat and copying files.
     *
     * @return A String that converts the java <code>"com.example.java.package"</code> to
     * <code>"com/example/java/package"</code>
     */
    public static String getMPFolder() {
        return ((String) getPref(MODULE_PACKAGE)).replaceAll("\\.", "/");
    }
}
