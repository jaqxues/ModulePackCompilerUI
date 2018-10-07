package com.jaqxues.modulepackcompilerui;

import java.util.ArrayList;
import java.util.List;

/**
 * This file was created by Jacques (jaqxues) in the Project ModulePackCompilerUI.<br>
 * Date: 07.10.2018 - Time 11:21.
 */

public enum PreferencesDef {
    PROJECT_ROOT("ProjectRoot", String.class, "."),
    MODULE_PACKAGE("ModulePackage", String.class, null),
    ATTRIBUTES("Attributes", List.class, new ArrayList<String[]>()),
    SIGN_PACK("SignPack", Boolean.class, true);

    private String key;
    private Class<?> type;
    private Object defaultValue;

    PreferencesDef(String key, Class type, Object defaultValue) {
        this.key = key;
        this.type = type;
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
}
