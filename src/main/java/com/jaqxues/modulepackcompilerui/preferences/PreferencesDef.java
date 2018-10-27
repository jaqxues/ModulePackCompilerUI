package com.jaqxues.modulepackcompilerui.preferences;

import com.google.gson.reflect.TypeToken;
import com.jaqxues.modulepackcompilerui.models.SignConfigModel;
import com.jaqxues.modulepackcompilerui.models.VirtualAdbDeviceModel;
import com.jaqxues.modulepackcompilerui.utils.BooleanPair;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javafx.util.Pair;

/**
 * This file was created by Jacques (jaqxues) in the Project ModulePackCompilerUI.<br>
 * Date: 07.10.2018 - Time 11:21.
 */

public enum PreferencesDef {
    DARK_THEME("DarkTheme", Boolean.class, false),
    PROJECT_ROOT("ProjectRoot", String.class, null),
    MODULE_PACKAGE("ModulePackage", String.class, null),
    FILE_SOURCES("FileSources", new TypeToken<List<BooleanPair<String>>>() {}.getType(), null),
    ADB_PUSH_TOGGLE("AdbPushToggle", Boolean.class, false),
    ATTRIBUTES("Attributes", ArrayList.class, new ArrayList<String>()),
    SIGN_PACK("SignPack", Boolean.class, true),
    SIGN_CONFIGS("SingConfigs", new TypeToken<List<SignConfigModel>>() {}.getType(), new ArrayList<SignConfigModel>()),
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
