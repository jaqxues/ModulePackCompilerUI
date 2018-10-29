package com.jaqxues.modulepackcompilerui.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.jaqxues.modulepackcompilerui.models.VirtualAdbDeviceModel;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import se.vidstige.jadb.DeviceDetectionListener;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;

/**
 * This file was created by Jacques (jaqxues) in the Project ModulePackCompilerUI.<br>
 * Date: 26.10.2018 - Time 16:39.
 */

public class AdbUtils {

    public static final String JSON_FILE = "Files/Models/AdbDevices.json";
    private static ObservableList<VirtualAdbDeviceModel> adbDevices = FXCollections.observableList(new ArrayList<>());
    private static boolean isInitialized = false;
    private static JadbConnection connection = new JadbConnection();

    public static ObservableList<VirtualAdbDeviceModel> getDevices() {
        init();
        return adbDevices;
    }

    public static void init() {
        if (isInitialized)
            return;
        isInitialized = true;
        LogUtils.getLogger().debug("Starting Adb-Server");
        JsonArray array = getConfigJson();
        if (array.size() != 0) {
            for (int i = 0; i < array.size(); i++)
                adbDevices.add(GsonSingleton.getSingleton().fromJson(array.get(i), VirtualAdbDeviceModel.class));
        }
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
                    if (devices == null || devices.isEmpty())
                        return;
                    LogUtils.getLogger().debug("New devices have been detected." + Arrays.deepToString(devices.toArray()));
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
            }).run();
            LogUtils.getLogger().debug("Successfully setup device watcher");
        } catch (Exception e) {
            LogUtils.getLogger().error("Unable to setup device watcher", e);
        }
    }

    public static boolean removeDevice(VirtualAdbDeviceModel model) {
        adbDevices.remove(model);
        JsonArray array = getConfigJson();
        Iterator<JsonElement> iterator = array.iterator();
        while (iterator.hasNext())
            if (iterator.next().getAsJsonObject().get("Serial").getAsString().equals(model.getSerial())) {
                iterator.remove();
                return true;
            }
        return false;
    }

    public static List<VirtualAdbDeviceModel> refresh() {
        for (VirtualAdbDeviceModel virtualAdbDeviceModel : adbDevices) {
                virtualAdbDeviceModel.refresh();
        }
        bindDevices(getConnectedDevices(), adbDevices);
        saveJson(GsonSingleton.getSingleton().toJsonTree(adbDevices));
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
        if (newDevs != 0)
            saveJson(GsonSingleton.getSingleton().toJsonTree(adbDevices));
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
            LogUtils.getLogger().error("Could not parse AdbDevices json", e);
        } catch (JsonParseException | IllegalStateException e) {
            LogUtils.getLogger().error("AdbDevices Json Corrupted, unable to parse file", e);
            overwriteFile();
        }
        return new JsonArray();
    }

    private static void overwriteFile() {
        try (FileWriter writer = new FileWriter(JSON_FILE)) {
            writer.write("[]");
            writer.flush();
        } catch (IOException e) {
            LogUtils.getLogger().error("Unable to over-write corrupted Json File", e);
        }
    }

    private static void saveJson(JsonElement element) {
        try (FileWriter writer = new FileWriter(JSON_FILE)) {
            writer.write(element.toString());
            writer.flush();
        } catch (IOException e) {
            LogUtils.getLogger().error("Unable to save AdbUtils Json", e);
        }
    }
}
