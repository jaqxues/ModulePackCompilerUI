package com.jaqxues.modulepackcompilerui.utils;

import com.jaqxues.modulepackcompilerui.models.VirtualAdbDeviceModel;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javafx.scene.control.Alert;
import se.vidstige.jadb.DeviceDetectionListener;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;

/**
 * This file was created by Jacques (jaqxues) in the Project ModulePackCompilerUI.<br>
 * Date: 26.10.2018 - Time 16:39.
 */

public class AdbUtils {
    private static List<VirtualAdbDeviceModel> adbDevices = new ArrayList<>();
    private static boolean isInitialized = false;
    private static JadbConnection connection = new JadbConnection();

    public static List<VirtualAdbDeviceModel> getDevices() {
        if (!isInitialized) {
            bindDevices(getConnectedDevices(), adbDevices);
            setWatcher();
        }
        isInitialized = true;
        return adbDevices;
    }

    private static List<JadbDevice> getConnectedDevices() {
        try {
            return connection.getDevices();
        } catch (Exception e) {
            // ConnectException is thrown if no Phone is connected --> can be ignored
            if (!(e instanceof ConnectException))
                LogUtils.getLogger().error("Error fetching JadbDevices");
        }
        return Collections.emptyList();
    }

    private static void setWatcher() {
        try {
            connection.createDeviceWatcher(new DeviceDetectionListener() {
                @Override
                public void onDetect(List<JadbDevice> devices) {
                    LogUtils.getLogger().debug("New devices have been detected.", Arrays.deepToString(devices.toArray()));
                    if (bindDevices(devices, adbDevices) > 0) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Adb Settings");
                        alert.setHeaderText("New Device detected");
                        alert.setContentText("A new Device has been connected to the PC via Adb");
                        alert.show();
                    }
                }

                @Override
                public void onException(Exception e) {
                    LogUtils.getLogger().error("Exception in DeviceDetectionListener", e);
                }
            });
            LogUtils.getLogger().debug("Successfully setup device watcher");
        } catch (Exception e) {
            LogUtils.getLogger().error("Unable to setup device watcher", e);
        }
    }

    /**
     * Helper method to bind a list of connected JadbDevices to a static List which contains every
     * Devices that has ever been connected to this PC. (Adds a new Device if necessary)
     *
     * @param jadbDevices The list of {@link JadbDevice} Objects.
     * @param vAdbDevices The list of all {@link VirtualAdbDeviceModel} Objects that have ever been
     *                    connected
     * @return The number of new Devices that have been detected.
     */
    private static int bindDevices(List<JadbDevice> jadbDevices, List<VirtualAdbDeviceModel> vAdbDevices) {
        int newDevs = 0;
        for (JadbDevice device : jadbDevices) {
            boolean found = false;
            for (VirtualAdbDeviceModel adbDevice : vAdbDevices) {
                if (device.getSerial().equals(adbDevice.getSerial())) {
                    adbDevice.setDevice(device);
                    found = true;
                    break;
                }
            }
            if (!found) {
                newDevs++;
                adbDevices.add(new VirtualAdbDeviceModel(device));
            }
        }
        return newDevs;
    }


}
