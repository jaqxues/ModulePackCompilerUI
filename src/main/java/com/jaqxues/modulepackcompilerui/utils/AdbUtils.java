package com.jaqxues.modulepackcompilerui.utils;

import com.jaqxues.modulepackcompilerui.models.VirtualAdbDeviceModel;

import java.io.IOException;
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
        init();
        return adbDevices;
    }

    public static void init() {
        if (isInitialized)
            return;
        isInitialized = true;
        LogUtils.getLogger().debug("Starting Adb-Server");
        if (startServer()) {
            LogUtils.getLogger().debug("Initializing AdbDevices and Device Watcher");
            bindDevices(getConnectedDevices(), adbDevices);
            setWatcher();
        } else {
            MiscUtils.showAlert(
                    Alert.AlertType.ERROR,
                    "Adb Settings",
                    "Check Adb Installation",
                    "To use the Adb Feature, please check your Adb installation.\n\nThe program was unable to start the Adb server (\"adb start-server\" command)"
            );
        }
    }

    private static List<JadbDevice> getConnectedDevices() {
        try {
            return connection.getDevices();
        } catch (Exception e) {
            LogUtils.getLogger().error(null, e);
        }
        return Collections.emptyList();
    }

    private static void setWatcher() {
        try {
            if (Runtime.getRuntime().exec("adb start-server").waitFor() != 0) {
                MiscUtils.showAlert(
                        Alert.AlertType.ERROR,
                        "Adb Settings",
                        "Unable to create Device Watcher",
                        "New Devices will not be detected without the device watcher.\n\nAdb probably won't work for you, please check your Adb Installation"
                );
                return;
            }
            connection.createDeviceWatcher(new DeviceDetectionListener() {
                @Override
                public void onDetect(List<JadbDevice> devices) {
                    LogUtils.getLogger().debug("New devices have been detected.", Arrays.deepToString(devices.toArray()));
                    if (bindDevices(devices, adbDevices) > 0) {
                        MiscUtils.showAlert(
                                Alert.AlertType.INFORMATION,
                                "Adb Settings",
                                "New Device detected",
                                "A new Device has been connected to the PC via Adb"
                        );
                    }
                }

                @Override
                public void onException(Exception e) {
                    LogUtils.getLogger().error("Exception in DeviceDetectionListener", e);
                }
            });
            LogUtils.getLogger().debug("Successfully setup device watcher");
        } catch (Exception e) {
//            if (!(e instanceof ConnectException))
            LogUtils.getLogger().error("Unable to setup device watcher", e);
        }
    }

    public static List<VirtualAdbDeviceModel> refresh() {
        isInitialized = false;
        return getDevices();
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

    /**
     * Jadb requires the Adb server to be started before using it. This is just a simple error
     * handling method to ensure it has been started correctly
     *
     * @return True if the server has been started
     */
    private static boolean startServer() {
        try {
            return Runtime.getRuntime().exec("adb start-server").waitFor() == 0;
        } catch (IOException | InterruptedException e) {
            LogUtils.getLogger().error("Unable to start adb server", e);
        }
        return false;
    }
}
