package com.jaqxues.modulepackcompilerui;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.control.TableCell;

/**
 * This file was created by Jacques (jaqxues) in the Project ModulePackCompilerUI.<br>
 * Date: 07.10.2018 - Time 11:21.
 */

public enum PreferencesDef {
    PROJECT_ROOT("ProjectRoot", String.class, "."),
    MODULE_PACKAGE("ModulePackage", String.class, null),
    FILE_SOURCES("FileSources", ArrayList.class, null),
    ATTRIBUTES("Attributes", ArrayList.class, new ArrayList<String>()),
    SIGN_PACK("SignPack", Boolean.class, true),
    SIGN_CONFIGS("SingConfigs", new TypeToken<List<SignConfig>>() {}.getType(), new ArrayList<SignConfig>()),
    SELECTED_SIGN_CONFIG("SelectedSignConfig", SignConfig.class, null),
    SDK_BUILD_TOOLS("SdkBuildTools", String.class, null),
    JDK_INSTALLATION_PATH("JDKInstallation", String.class, null);

    private String key;
    private Class<?> type;
    private Object defaultValue;
    private Type typeToken;

    PreferencesDef(String key, Class type, Object defaultValue) {
        this.key = key;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    PreferencesDef(String key, Type type, Object defaultValue) {
        this.key = key;
        this.typeToken = type;
        this.defaultValue = defaultValue;
    }

    public static PreferencesDef fromKey(String key) {
        for (PreferencesDef preferencesDef : PreferencesDef.values()) {
            if (preferencesDef.key.equals(key))
                return preferencesDef;
        }
        return null;
    }

    public String getKey() {
        return key;
    }

    public Class<?> getType() {
        return type;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public Type getTypeToken() {
        return typeToken;
    }
}
