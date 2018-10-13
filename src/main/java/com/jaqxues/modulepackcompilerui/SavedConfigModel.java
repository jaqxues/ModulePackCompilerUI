package com.jaqxues.modulepackcompilerui;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * This file was created by Jacques (jaqxues) in the Project ModulePackCompilerUI.<br>
 * Date: 13.10.2018 - Time 09:34.
 */

public class SavedConfigModel {

    private static final String JSON_FILE = "SavedConfig.json";

    @SerializedName("SavedConfigName")
    public String savedConfigName;
    @SerializedName("SavedConfigNotices")
    public String savedConfigNotices;
    @SerializedName("SavedConfigDate")
    public long savedConfigDate;
    @SerializedName("SignConfig")
    public SignConfig signConfig;
    @SerializedName("ProjectRoot")
    public String projectRoot;
    @SerializedName("Module Package")
    public String modulePackage;
    @SerializedName("Module Sources")
    public List<String> moduleSources;
    @SerializedName("Attributes")
    public List<String> attributes;

    public static void addConfig(SavedConfigModel model) {
        JsonObject object = getConfigJson();
        object.addProperty(model.getSavedConfigName(), GsonSingleton.getSingleton().toJson(model));
        saveJson(object);
    }

    public static void removeConfig(SavedConfigModel model) {
        JsonObject object = getConfigJson();
        object.remove(model.getSavedConfigName());
        saveJson(object);
    }

    private static void saveJson(JsonElement element) {
        try (FileWriter writer = new FileWriter(JSON_FILE)) {
            writer.write(element.toString());
            writer.flush();
        } catch (IOException e) {
            LogUtils.getLogger().error("Unable to save SavedConfig Json", e);
        }
    }

    private static JsonObject getConfigJson() {
        JsonParser jsonParser = new JsonParser();
        File file = new File(JSON_FILE);
        try {
            if (!file.exists())
                file.createNewFile();

            FileReader reader = new FileReader(JSON_FILE);
            return jsonParser.parse(reader).getAsJsonObject();
        } catch (IOException e) {
            LogUtils.getLogger().error("Could not parse SavedConfig json", e);
        } catch (JsonParseException e) {
            LogUtils.getLogger().error("SavedConfig Json Corrupted, unable to parse file", e);
            try (FileWriter writer = new FileWriter(JSON_FILE)) {
                writer.write("{}");
                writer.flush();
            } catch (IOException e2) {
                LogUtils.getLogger().error("Unable to over-write corrupted Json File", e2);
            }
        }
        return new JsonObject();
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

    public SignConfig getSignConfig() {
        return signConfig;
    }

    public SavedConfigModel setSignConfig(SignConfig signConfig) {
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

    public List<String> getModuleSources() {
        return moduleSources;
    }

    public SavedConfigModel setModuleSources(List<String> moduleSources) {
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
