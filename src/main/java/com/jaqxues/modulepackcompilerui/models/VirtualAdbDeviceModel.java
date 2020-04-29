package com.jaqxues.modulepackcompilerui.models;

import com.google.common.base.Objects;
import com.google.gson.annotations.SerializedName;
import com.jaqxues.modulepackcompilerui.utils.ExcludeSerialization;
import com.jaqxues.modulepackcompilerui.utils.RowCellFactory;

import se.vidstige.jadb.JadbDevice;

/**
 * This file was created by Jacques (jaqxues) in the Project ModulePackCompilerUI.<br>
 * Date: 26.10.2018 - Time 17:18.
 */

public class VirtualAdbDeviceModel implements RowCellFactory.ColorStateManager {

    public static final String PUSH_PATH_DEFAULT = "/storage/emulated/0/SnapTools/ModulePacks/";
    @SerializedName("Name")
    private String name;
    @SerializedName("Serial")
    private String serial;
    @SerializedName("PushPath")
    private String pushPath;
    @ExcludeSerialization
    private boolean isConnected;
    @SerializedName("IsActive")
    private boolean isActive;
    @ExcludeSerialization
    private JadbDevice device;

    public VirtualAdbDeviceModel(JadbDevice jadbDevice) {
        this.name = jadbDevice.toString();
        this.serial = jadbDevice.getSerial();
        this.pushPath = PUSH_PATH_DEFAULT;
        this.isConnected = true;
        this.device = jadbDevice;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getPushPath() {
        return pushPath;
    }

    public void setPushPath(String pushPath) {
        this.pushPath = pushPath;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public boolean setActive(boolean isActive) {
        this.isActive = isActive;
        return isActive;
    }

    public boolean isActive() {
        return isActive;
    }

    public JadbDevice getDevice() {
        return device;
    }

    public void setDevice(JadbDevice jadbDevice) {
        if (jadbDevice == null) {
            this.device = null;
            isConnected = false;
            return;
        }
        if (!jadbDevice.getSerial().equals(serial))
            throw new IllegalArgumentException("Serial Number not corresponding current Virtual Device");
        this.device = jadbDevice;
        isConnected = true;
    }

    public void refresh() {
        if (device == null)
            isConnected = false;
    }

    @Override
    public boolean equals(Object model) {
        if (!(model instanceof VirtualAdbDeviceModel))
            return false;
        return Objects.equal(serial, ((VirtualAdbDeviceModel) model).serial);
    }

    @Override
    public int tableRowColor() {
        if (!isActive) return 0;
        int i = 3;
        if (isConnected) i--;
        if (device != null) i--;
        return i;
    }

    @Override
    public String getString() {
        return "VirtualAdbDeviceModel{" +
                "name='" + name + '\'' +
                ", serial='" + serial + '\'' +
                ", pushPath='" + pushPath + '\'' +
                ", isConnected=" + isConnected +
                ", isActive=" + isActive +
                ", device=" + device +
                '}';
    }

    public String getDescription() {
        return name + " (" + serial + ")";
    }
}
