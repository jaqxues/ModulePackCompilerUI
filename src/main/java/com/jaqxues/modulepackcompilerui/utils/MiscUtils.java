package com.jaqxues.modulepackcompilerui.utils;

import java.util.Map;

import javax.annotation.CheckReturnValue;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.layout.Region;

import static com.jaqxues.modulepackcompilerui.preferences.PreferenceManager.getPref;
import static com.jaqxues.modulepackcompilerui.preferences.PreferencesDef.MODULE_PACKAGE;

/**
 * This file was created by Jacques (jaqxues) in the Project ModulePackCompilerUI.<br>
 * Date: 18.10.2018 - Time 15:32.
 */

public class MiscUtils {

    /**
     * Used instead of having duplicates throughout the project for dx.bat and copying files.
     *
     * @return A String that converted from <code>"com.example.java.package"</code> to
     * <code>"com/example/java/package"</code>
     */
    public static String getMPFolder() {
        return ((String) getPref(MODULE_PACKAGE)).replaceAll("\\.", "/");
    }

    public static String getLocalAppDataDir() {
        return System.getenv("LOCALAPPDATA");
    }

    public static String getProgramFilesDir() {
        return System.getenv("ProgramFiles");
    }

    /**
     * Shows a JavaFX alert from any thread.
     *
     * @param alertType Sets the alert type of the error
     * @param title     Title of the Alert
     * @param header    HeaderText of the Alert
     * @param message   ContentText of the Alert
     */
    public static void showAlert(Alert.AlertType alertType, String title, String header, String message) {
        showAlert(alertType, title, header, message, -1d, -1d);
    }
    public static void showAlert(Alert.AlertType alertType, String title, String header, String message, double width, double height) {
        Runnable runnable = () -> {
            Alert alert = new Alert(alertType);
            if (width != -1 && height != -1) {
                alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                alert.getDialogPane().setPrefSize(width, height);
                alert.setHeight(height);
                alert.setWidth(width);
            }
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(message);
            alert.show();
        };
        if (Platform.isFxApplicationThread())
            runnable.run();
        else
            Platform.runLater(runnable);
    }

    public static <T> void temporaryDisable(ReadOnlyObjectProperty<T> property, Node... nodes) {
        for (Node node : nodes)
            node.setDisable(true);
        property.addListener(new ChangeListener<T>() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                property.removeListener(this);
                for (Node node : nodes)
                    node.setDisable(false);
            }
        });
    }

    @CheckReturnValue
    public static <T, K> Map.Entry<T, K> getEntryFromMap(Map<T, K> map, T key) {
        for (Map.Entry<T, K> entry : map.entrySet())
            if (entry.getKey().equals(key))
                return entry;
        return null;
    }
}
