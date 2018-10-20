package com.jaqxues.modulepackcompilerui.models;

import com.google.gson.annotations.SerializedName;

/**
 * This file was created by Jacques (jaqxues) in the Project ModulePackCompilerUI.<br>
 * Date: 10.10.2018 - Time 18:24.
 */

public class SignConfig {

    @SerializedName("StorePath")
    private String storePath;

    @SerializedName("StorePassword")
    private String storePassword;

    @SerializedName("KeyAlias")
    private String keyAlias;

    @SerializedName("KeyPassword")
    private String keyPassword;

    @SerializedName("isActive")
    private boolean isActive;

    public SignConfig(String storePath, String storePassword, String keyAlias, String keyPassword) {
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

    public String getKeyAlias() {
        return keyAlias;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    public boolean isActivated() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
}
