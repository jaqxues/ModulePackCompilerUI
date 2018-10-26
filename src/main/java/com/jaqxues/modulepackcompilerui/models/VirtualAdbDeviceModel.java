package com.jaqxues.modulepackcompilerui.models;

import com.google.common.base.Objects;
import com.google.gson.annotations.SerializedName;

import se.vidstige.jadb.JadbDevice;

/**
 * This file was created by Jacques (jaqxues) in the Project ModulePackCompilerUI.<br>
 * Date: 26.10.2018 - Time 17:18.
 */

public class VirtualAdbDeviceModel {

    @SerializedName("Serial")
    private String serial;
    @SerializedName("PushPath")
    private String pushPath;
    @SerializedName("IsConnected")
    private boolean isConnected;
    private JadbDevice device;

    public static final String PUSH_PATH_DEFAULT = "/sdcard/ModulePack/";

    public VirtualAdbDeviceModel() {}

    public VirtualAdbDeviceModel(JadbDevice jadbDevice) {
        this.serial = jadbDevice.getSerial();
        this.pushPath = PUSH_PATH_DEFAULT;
        this.isConnected = true;
        this.device = jadbDevice;
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

    public JadbDevice getDevice() {
        return device;
    }

    @Override
    public boolean equals(Object model) {
        if (!(model instanceof VirtualAdbDeviceModel))
            return false;
        return Objects.equal(serial, ((VirtualAdbDeviceModel) model).serial);
    }
}
