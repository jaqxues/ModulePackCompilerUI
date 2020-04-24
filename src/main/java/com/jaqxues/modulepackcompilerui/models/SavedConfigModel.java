package com.jaqxues.modulepackcompilerui.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import com.jaqxues.modulepackcompilerui.utils.GsonSingleton;
import com.jaqxues.modulepackcompilerui.utils.LogUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * This file was created by Jacques (jaqxues) in the Project ModulePackCompilerUI.<br>
 * Date: 13.10.2018 - Time 09:34.
 */

public class SavedConfigModel {

    private static final String JSON_FILE = "Files/Models/SavedConfig.json";

    @SerializedName("SavedConfigName")
    public String savedConfigName;
    @SerializedName("SavedConfigNotices")
    public String savedConfigNotices;
    @SerializedName("SavedConfigDate")
    public long savedConfigDate;
    @SerializedName("SignConfigModel")
    public SignConfigModel signConfig;
    @SerializedName("ProjectRoot")
    public String projectRoot;
    @SerializedName("ModulePackage")
    public String modulePackage;
    @SerializedName("ModuleSources")
    public Map<String, Boolean> moduleSources;
    @SerializedName("Attributes")
    public List<String> attributes;


    /**
     * @param model
     * @return True if the SignConfigModel has been added to the Saved Configs, False if the name already existed
     */
    public static boolean addConfig(SavedConfigModel model) {
        JsonArray object = getConfigJson();
        for (JsonElement element : object) {
            if (element.getAsJsonObject().get("SavedConfigName").getAsString().equals(model.getSavedConfigName()))
                return false;
        }
        object.add(GsonSingleton.getSingleton().toJsonTree(model));
        saveJson(object);
        return true;
    }

    public static void removeConfig(SavedConfigModel model) {
        if (model == null)
            return;
        JsonArray object = getConfigJson();
        Iterator<JsonElement> iterator = object.iterator();
        while (iterator.hasNext()) {
            JsonElement element = iterator.next();
            if (element.getAsJsonObject().get("SavedConfigName").getAsString().equals(model.getSavedConfigName())) {
                iterator.remove();
                break;
            }
        }
        saveJson(object);
    }

    public static SavedConfigModel[] getConfigs() {
        JsonArray object = getConfigJson();
        if (object.size() == 0) {
            SavedConfigModel[] savedConfigModels = new SavedConfigModel[]{
                    getDefaultSnapToolsSignConfig(),
                    getDefaultAkrolybSignConfig()
            };
            saveJson(GsonSingleton.getSingleton().toJsonTree(savedConfigModels));
            return savedConfigModels;
        }
        SavedConfigModel[] array = new SavedConfigModel[object.size()];
        for (int i = 0; i < object.size(); i++) {
            array[i] = GsonSingleton.getSingleton().fromJson(object.get(i), SavedConfigModel.class);
        }
        return array;
    }

    private static void saveJson(JsonElement element) {
        try (FileWriter writer = new FileWriter(JSON_FILE)) {
            writer.write(element.toString());
            writer.flush();
        } catch (IOException e) {
            LogUtils.getLogger().error("Unable to save SavedConfig Json", e);
        }
    }

    private static SavedConfigModel getDefaultSnapToolsSignConfig() {
        Map<String, Boolean> sources = new HashMap<>();
        sources.put("/app/build/tmp/kotlin-classes/packRelease/", true);
        sources.put("/app/build/intermediates/javac/packRelease/classes/", true);
        return new SavedConfigModel()
                .setModulePackage("com.ljmu.andre.snaptools.ModulePack")
                .setSavedConfigDate(System.currentTimeMillis())
                .setSavedConfigName("Default SnapTools Configuration")
                .setAttributes(new ArrayList<>(Arrays.asList(
                        "Development=TRUE",
                        "PackVersion=1.0.0.0",
                        "Flavour=prod",
                        "Type=Premium",
                        "SCVersion=10.41.6.0"
                )))
                .setSavedConfigDate(System.currentTimeMillis())
                .setSavedConfigNotices("Default Configuration for a SnapTools ModulePack")
                .setModuleSources(sources);
    }

    private static SavedConfigModel getDefaultAkrolybSignConfig() {
        Map<String, Boolean> sources = new HashMap<>();
        sources.put("/app/build/tmp/kotlin-classes/packRelease/", true);
        return new SavedConfigModel()
                .setModulePackage("com.jaqxues.akrolyb.sample.pack")
                .setSavedConfigDate(System.currentTimeMillis())
                .setSavedConfigName("Default Akrolyb Configuration")
                .setAttributes(new ArrayList<>(Arrays.asList(
                        "Development=TRUE",
                        "PackVersionCode=1",
                        "PackVersion=1.0.0",
                        "MinApkVersionCode=1",
                        "PackImpl=com.jaqxues.akrolyb.sample.pack.PackImpl"
                )))
                .setSavedConfigDate(System.currentTimeMillis())
                .setSavedConfigNotices("Default Configuration for an Akrolyb Pack")
                .setModuleSources(sources);
    }


    private static JsonArray getConfigJson() {
        JsonParser jsonParser = new JsonParser();
        File file = new File(JSON_FILE);
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
                overwriteFile();
            }

            FileReader reader = new FileReader(JSON_FILE);
            return jsonParser.parse(reader).getAsJsonArray();
        } catch (IOException e) {
            LogUtils.getLogger().error("Could not parse SavedConfig json", e);
        } catch (JsonParseException | IllegalStateException e) {
            LogUtils.getLogger().error("SavedConfig Json Corrupted, unable to parse file", e);
            overwriteFile();
        }
        return new JsonArray();
    }

    private static void overwriteFile() {
        try (FileWriter writer = new FileWriter(JSON_FILE)) {
            writer.write("[]");
            writer.flush();
        } catch (IOException e2) {
            LogUtils.getLogger().error("Unable to over-write corrupted Json File", e2);
        }
    }

    public String getSavedConfigName() {
        return savedConfigName;
    }

    public SavedConfigModel setSavedConfigName(String savedConfigName) {
        this.savedConfigName = savedConfigName;
        return this;
    }

    public String getSavedConfigNotices() {
        return savedConfigNotices;
    }

    public SavedConfigModel setSavedConfigNotices(String savedConfigNotices) {
        this.savedConfigNotices = savedConfigNotices;
        return this;
    }

    public long getSavedConfigDate() {
        return savedConfigDate;
    }

    public SavedConfigModel setSavedConfigDate(long savedConfigDate) {
        this.savedConfigDate = savedConfigDate;
        return this;
    }

    public SignConfigModel getSignConfig() {
        return signConfig;
    }

    public SavedConfigModel setSignConfig(SignConfigModel signConfig) {
        this.signConfig = signConfig;
        return this;
    }

    public String getProjectRoot() {
        return projectRoot;
    }

    public SavedConfigModel setProjectRoot(String projectRoot) {
        this.projectRoot = projectRoot;
        return this;
    }

    public String getModulePackage() {
        return modulePackage;
    }

    public SavedConfigModel setModulePackage(String modulePackage) {
        this.modulePackage = modulePackage;
        return this;
    }

    public Map<String, Boolean> getModuleSources() {
        return moduleSources;
    }

    public SavedConfigModel setModuleSources(Map<String, Boolean> moduleSources) {
        this.moduleSources = moduleSources;
        return this;
    }

    public List<String> getAttributes() {
        return attributes;
    }

    public SavedConfigModel setAttributes(List<String> attributes) {
        this.attributes = attributes;
        return this;
    }
}
