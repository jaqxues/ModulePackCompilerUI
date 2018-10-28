package com.jaqxues.modulepackcompilerui.models;

import com.google.common.base.MoreObjects;
import com.google.gson.annotations.SerializedName;
import com.jaqxues.modulepackcompilerui.utils.RowCellFactory;

/**
 * This file was created by Jacques (jaqxues) in the Project ModulePackCompilerUI.<br>
 * Date: 10.10.2018 - Time 18:24.
 */

public class SignConfigModel implements RowCellFactory.ActiveStateManager {

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

    public String getKeyAlias() {
        return keyAlias;
    }

    public String getKeyPassword() {
        return keyPassword;
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
        return toString();
    }

    @Override
    public String toString() {
        return "SignConfigModel{" +
                "storePath='" + storePath + '\'' +
                ", storePassword='" + storePassword + '\'' +
                ", keyAlias='" + keyAlias + '\'' +
                ", keyPassword='" + keyPassword + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
