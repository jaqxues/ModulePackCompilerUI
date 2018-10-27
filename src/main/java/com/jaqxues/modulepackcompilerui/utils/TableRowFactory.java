package com.jaqxues.modulepackcompilerui.utils;

import javafx.scene.control.TableRow;

import static com.jaqxues.modulepackcompilerui.preferences.PreferenceManager.getPref;
import static com.jaqxues.modulepackcompilerui.preferences.PreferencesDef.DARK_THEME;

/**
 * This file was created by Jacques (jaqxues) in the Project ModulePackCompilerUI.<br>
 * Date: 26.10.2018 - Time 23:50.
 */

public class TableRowFactory {

    public static <T extends ActiveStateManager> TableRow<T> getAStateTableRow() {
        return new TableRow<T>() {
            private final String getColor(boolean selected) {
                if (selected)
                    return getPref(DARK_THEME) ? "darkgreen" : "green";
                return getPref(DARK_THEME) ? "green" : "lightgreen";
            }
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && item.active()) {
                    setStyle("-fx-background-color: " + getColor(false));
                    selectedProperty().addListener((observable, oldValue, newValue) -> setStyle("-fx-background-color: " + getColor(newValue)));
                }
            }
        };
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
                if (item == null || item.tableRowColor() == 0)
                    return;
                switch (item.tableRowColor()) {
                    case 3:
                        setBgRow("red", "darkred");
                        break;
                    case 2:
                        setBgRow("orange", "darkorange");
                        break;
                    case 1:
                        setBgRow("lightgreen", "green");
                        break;
                    default:
                        LogUtils.getLogger().error("Unknown Color Int, " + item.tableRowColor());
                }
            }
        };
    }

    public interface ActiveStateManager {
        boolean active();
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
    }
}
