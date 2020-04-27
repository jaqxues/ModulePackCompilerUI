package com.jaqxues.modulepackcompilerui.utils;

import java.io.File;
import java.util.Map;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableRow;

import static com.jaqxues.modulepackcompilerui.preferences.PreferenceManager.getPref;
import static com.jaqxues.modulepackcompilerui.preferences.PreferencesDef.DARK_THEME;
import static com.jaqxues.modulepackcompilerui.preferences.PreferencesDef.PROJECT_ROOT;

/**
 * This file was created by Jacques (jaqxues) in the Project ModulePackCompilerUI.<br>
 * Date: 26.10.2018 - Time 23:50.
 */

public class RowCellFactory {

    public static <T extends ActiveStateManager> TableRow<T> getAStateTableRow() {
        return new TableRow<T>() {
            private String getColor(boolean selected) {
                if (selected)
                    return getPref(DARK_THEME) ? "darkgreen" : "green";
                return getPref(DARK_THEME) ? "green" : "lightgreen";
            }
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty)
                    return;
                setText(item.getString());
                if (item.active()) {
                    setStyle("-fx-background-color: " + getColor(false));
                    selectedProperty().addListener((observable, oldValue, newValue) -> setStyle("-fx-background-color: " + getColor(newValue)));
                }
            }
        };
    }

    public static ListCell<Map.Entry<String, Boolean>> getSourcesListCell() {
        return new ListCell<Map.Entry<String, Boolean>>() {

            ChangeListener<? super Boolean> listener;

            private void setBgRow(String normalColor, String selectedColor) {
                if (listener != null)
                    selectedProperty().removeListener(listener);
                if (normalColor == null && selectedColor == null) {
                    setStyle(null);
                    return;
                }
                setStyle("-fx-background-color: " + normalColor);
                listener = (observable, oldValue, newValue) -> {
                    if (newValue)
                        setStyle("-fx-background-color: " + selectedColor);
                    else
                        setStyle("-fx-background-color: " + normalColor);
                };
                selectedProperty().addListener(listener);
            }

            @Override
            protected void updateItem(Map.Entry<String, Boolean> item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    // Reset style if item has been removed
                    setStyle(null);
                    setText("");
                    return;
                }
                setText(item.getKey());
                switch (sourceState(item)) {
                    case 3:
                        setBgRow(getPref(DARK_THEME) ? "red" : "rgba(255,0,6,0.64)", getPref(DARK_THEME) ? "darkred" : "red");
                        break;
                    case 2:
                        setBgRow("orange", "darkorange");
                        break;
                    case 1:
                        setBgRow(getPref(DARK_THEME) ? "green" : "lightgreen", getPref(DARK_THEME) ? "darkgreen" : "green");
                        break;
                    case 0:
                        setBgRow(null, null);
                        break;
                    default:
                        LogUtils.getLogger().error("Unknown Color Int, " + sourceState(item));
                }
            }
        };
    }

    private static int sourceState(Map.Entry<String, Boolean> entry) {
        return (entry.getValue() ? new File((String) getPref(PROJECT_ROOT)).exists() ? 1 : 3 : 0);
    }

    public static <T extends ColorStateManager> TableRow<T> getCStateTableRow() {
        return new TableRow<T>() {
            private void setBgRow(String normalColor, String selectedColor) {
                setStyle("-fx-background-color: " + normalColor);
                selectedProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue)
                        setStyle("-fx-background-color: " + selectedColor);
                    else
                        setStyle("-fx-background-color: " + normalColor);
                });
            }

            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty)
                    return;
                setText(item.getString());
                switch (item.tableRowColor()) {
                    case 3:
                        setBgRow(getPref(DARK_THEME) ? "red" : "rgba(255,0,6,0.64)", getPref(DARK_THEME) ? "darkred" : "red");
                        break;
                    case 2:
                        setBgRow("orange", "darkorange");
                        break;
                    case 1:
                        setBgRow("lightgreen", "green");
                        break;
                    case 0:
                        setStyle(null);
                        break;
                    default:
                        LogUtils.getLogger().error("Unknown Color Int, " + item.tableRowColor());
                }
            }
        };
    }

    public interface ActiveStateManager {
        boolean active();

        String getString();
    }

    public interface ColorStateManager {
        /**
         * In the TableRow this int is used to decide which background color it should use.
         *
         * <table>
         * <tr>
         * <th>Int Value</th>
         * <th>Color</th>
         * </tr>
         * <tr>
         * <td>0</td>
         * <td>White (No Background Color)</td>
         * </tr>
         * <tr>
         * <td>1</td>
         * <td>Green</td>
         * </tr>
         * <tr>
         * <td>2</td>
         * <td>Orange</td>
         * </tr>
         * <tr>
         * <td>3</td>
         * <td>Red</td>
         * </tr>
         * </table>
         *
         * @return An int between 0 and 3.
         */
        int tableRowColor();

        String getString();
    }
}
