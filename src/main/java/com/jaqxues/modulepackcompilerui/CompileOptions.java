package com.jaqxues.modulepackcompilerui;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * This file was created by Jacques (jaqxues) in the Project ModulePackCompilerUI.<br>
 * Date: 10.10.2018 - Time 18:19.
 */

public class CompileOptions {

    @SerializedName("KotlinSources")
    private String kotlinSource;

    @SerializedName("JavaSources")
    private String javaSources;

    @SerializedName("SignPack")
    private boolean signPack;

    @SerializedName("SignConfig")
    private SignConfig signConfig;


}
