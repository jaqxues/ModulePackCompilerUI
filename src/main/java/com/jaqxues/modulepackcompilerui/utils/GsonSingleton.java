package com.jaqxues.modulepackcompilerui.utils;

import com.google.gson.Gson;

/**
 * This file was created by Jacques (jaqxues) in the Project ModulePackCompilerUI.<br>
 * Date: 07.10.2018 - Time 11:32.
 */

public class GsonSingleton {
    private static Gson gson = new Gson();

    public static Gson getSingleton() {
        return gson;
    }
}
