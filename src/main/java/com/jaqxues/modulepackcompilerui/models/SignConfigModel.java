package com.jaqxues.modulepackcompilerui.models;

import com.google.gson.annotations.SerializedName;
import com.jaqxues.modulepackcompilerui.utils.RowCellFactory;

import static com.jaqxues.modulepackcompilerui.preferences.PreferenceManager.getPref;
import static com.jaqxues.modulepackcompilerui.preferences.PreferencesDef.SHOW_PASSWORDS;

/**
 * This file was created by Jacques (jaqxues) in the Project ModulePackCompilerUI.<br>
 * Date: 10.10.2018 - Time 18:24.
 */

public class SignConfigModel implements RowCellFactory.ActiveStateManager {

    public static final String PLACEHOLDER = "*********";

    @SerializedName("StorePath")
    private String storePath;

    @SerializedName("StorePassword")
    private String storePassword;

    @SerializedName("KeyAlias")
    private String keyAlias;

    @SerializedName("KeyPassword")
    private String keyPassword;

    @SerializedName("IsActive")
    private boolean isActive;

    public SignConfigModel(String storePath, String storePassword, String keyAlias, String keyPassword) {
        this.storePath = storePath;
        this.storePassword = storePassword;
        this.keyAlias = keyAlias;
        this.keyPassword = keyPassword;
        this.isActive = false;
    }

    public String getStorePath() {
        return storePath;
    }

    public String getStorePassword() {
        return storePassword;
    }

    public String getDisplayStorePassword() {
        return getPref(SHOW_PASSWORDS) ? storePassword : PLACEHOLDER;
    }

    public String getKeyAlias() {
        return keyAlias;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    public String getDisplayKeyPassword() {
        return getPref(SHOW_PASSWORDS) ? keyPassword : PLACEHOLDER;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public boolean active() {
        return isActive;
    }

    @Override
    public String getString() {
        return "Keystore Path: " + storePath +
                "\nKeystore Password: " + getDisplayStorePassword() +
                "\nKey Alias: " + keyAlias +
                "\nKey Password: " + getDisplayKeyPassword();
    }

    @Override
    public String toString() {
        return "SignConfigModel{" +
                "storePath='" + storePath + '\'' +
                ", storePassword='" + PLACEHOLDER + '\'' +
                ", keyAlias='" + keyAlias + '\'' +
                ", keyPassword='" + PLACEHOLDER + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
