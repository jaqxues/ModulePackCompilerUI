package com.jaqxues.modulepackcompilerui.preferences;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.jaqxues.modulepackcompilerui.utils.GsonSingleton;
import com.jaqxues.modulepackcompilerui.utils.LogUtils;

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
    private static final String FILE_NAME = "Files/Preferences.json";
    private static final Object MAP_LOCK = new Object();
    private static final Object WRITE_LOCK = new Object();
    private static File preferencesFile;
    private static Map<String, Object> preferences = new HashMap<>();

    public static void init() throws IOException {
        synchronized (MAP_LOCK) {
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
                                preferencesDef.getType() != null ? preferencesDef.getType() : preferencesDef.getTypeToken()
                        )
                );
            }

            for (PreferencesDef pref : PreferencesDef.values()) {
                if (preferences.containsKey(pref.getKey()))
                    continue;
                preferences.put(pref.getKey(), pref.getDefaultValue());
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void generateFile() throws IOException {
        synchronized (WRITE_LOCK) {
            preferencesFile.getParentFile().mkdirs();
            preferencesFile.createNewFile();
            FileWriter writer = new FileWriter(preferencesFile);
            writer.write("{}");
            writer.flush();
            writer.close();
        }
    }

    public static <T> T getPref(PreferencesDef pref) {
        synchronized (MAP_LOCK) {
            //noinspection unchecked
            return (T) preferences.get(pref.getKey());
        }
    }

    public static <T> T putPref(PreferencesDef pref, T value) {
        synchronized (MAP_LOCK) {
            preferences.put(pref.getKey(), value);
        }
        saveMap();
        return value;
    }

    public static void saveMap() {
        synchronized (WRITE_LOCK) {
            try (BufferedWriter writer = new BufferedWriter(
                    new FileWriter(preferencesFile)
            )) {
                synchronized (MAP_LOCK) {
                    writer.write(
                            GsonSingleton.getSingleton().toJson(preferences)
                    );
                }
                writer.flush();
            } catch (IOException e) {
                LogUtils.getLogger().error("Could not save Preferences Map", e);
            }
        }
    }

    @SuppressWarnings({"UnusedReturnValue", "unchecked"})
    public static <T> T addToCollection(PreferencesDef pref, T value) {
        synchronized (MAP_LOCK) {
            ((Collection<T>) getPref(pref)).add(value);
        }
        saveMap();
        return value;
    }

    public static <K, T> K addToMap(PreferencesDef pref, K key, T value) {
        synchronized (MAP_LOCK) {
            //noinspection unchecked
            ((Map<K, T>) getPref(pref)).put(key, value);
        }
        saveMap();
        return key;
    }

    public static <K, T> K removeFromMap(PreferencesDef pref, K key) {
        synchronized (MAP_LOCK) {
            //noinspection unchecked
            ((Map<K, T>) getPref(pref)).remove(key);
        }
        saveMap();
        return key;
    }

    public static <T> T removeFromCollection(PreferencesDef pref, T value) {
        synchronized (MAP_LOCK) {
            //noinspection unchecked
            ((Collection<T>) getPref(pref)).remove(value);
        }
        saveMap();
        return value;
    }

    public static void clearCollection(PreferencesDef pref) {
        synchronized (MAP_LOCK) {
            ((Collection<?>) getPref(pref)).clear();
        }
        saveMap();
    }

    public static void clearMap(PreferencesDef pref) {
        synchronized (MAP_LOCK) {
            ((Map) getPref(pref)).clear();
        }
        saveMap();
    }

    /**
     * Helper to toggle Boolean Preference
     *
     * @param pref The Preference to toggle
     * @return The new Boolean Value
     */
    public static boolean togglePref(PreferencesDef pref) {
        synchronized (MAP_LOCK) {
            boolean oldValue = getPref(pref);
            putPref(
                    pref,
                    !oldValue
            );
            return !oldValue;
        }
    }
}
