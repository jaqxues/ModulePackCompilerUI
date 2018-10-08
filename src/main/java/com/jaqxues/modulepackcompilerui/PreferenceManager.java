package com.jaqxues.modulepackcompilerui;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This file was created by Jacques (jaqxues) in the Project ModulePackCompilerUI.<br>
 * Date: 07.10.2018 - Time 10:57.
 */

public class PreferenceManager {
    private static final String FILE_NAME = "Preferences.json";
    private static final Object WRITE_LOCK = new Object();
    private static File preferencesFile;
    private static Map<String, Object> preferences = new HashMap<>(16);

    public static void init() throws IOException {
        preferencesFile = new File(FILE_NAME);
        if (!preferencesFile.exists())
            generateFile();
        FileReader reader = new FileReader(preferencesFile);
        JsonObject object;
        try {
            object = new JsonParser().parse(reader).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            LogUtils.getLogger().error("Corrupted Settings, Could not parse JSON: ", e);
            generateFile();
            init();
            return;
        }

        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            PreferencesDef preferencesDef = PreferencesDef.fromKey(entry.getKey());
            if (preferencesDef == null)
                continue;
            preferences.put(
                    entry.getKey(),
                    GsonSingleton.getSingleton().fromJson(
                            entry.getValue(),
                            preferencesDef.getType()
                    )
            );
        }

        for (PreferencesDef pref : PreferencesDef.values()) {
            if (preferences.containsKey(pref.getKey()))
                continue;
            preferences.put(pref.getKey(), pref.getDefaultValue());
        }
    }

    private static void generateFile() throws IOException {
        //noinspection ResultOfMethodCallIgnored
        preferencesFile.createNewFile();
        FileWriter writer = new FileWriter(preferencesFile);
        writer.write("{}");
        writer.flush();
        writer.close();
    }

    public static <T> T getPref(PreferencesDef pref) {
        //noinspection unchecked
        return (T) preferences.get(pref.getKey());
    }

    @SuppressWarnings("UnusedReturnValue")
    public static <T> T putPref(PreferencesDef pref, T value) {
        preferences.put(pref.getKey(), value);
        saveMap();
        return value;
    }

    private static void saveMap() {
        synchronized (WRITE_LOCK) {
            try (BufferedWriter writer = new BufferedWriter(
                    new FileWriter(preferencesFile)
            )) {
                writer.write(
                        GsonSingleton.getSingleton().toJson(preferences)
                );
                writer.flush();
            } catch (IOException e) {
                LogUtils.getLogger().error("Could not save Preferences Map", e);
            }
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public static <T> T addToCollection(PreferencesDef pref, T value) {
        ((Collection<T>) getPref(pref)).add(value);
        saveMap();
        return value;
    }

    public static <T> T removeFromCollection(PreferencesDef pref, T value) {
        ((Collection<?>) getPref(pref)).remove(value);
        saveMap();
        return value;
    }

    /**
     * Helper to toggle Boolean Preference
     *
     * @param pref The Preference to toggle
     * @return The new Boolean Value
     */
    public static boolean togglePref(PreferencesDef pref) {
        boolean oldValue = (boolean) getPref(pref);
        putPref(
                pref,
                !oldValue
        );
        return !oldValue;
    }
}