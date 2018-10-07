package com.jaqxues.modulepackcompilerui;

import com.sun.istack.internal.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;

import static com.jaqxues.modulepackcompilerui.PreferenceManager.getPref;
import static com.jaqxues.modulepackcompilerui.PreferenceManager.putPref;
import static com.jaqxues.modulepackcompilerui.PreferenceManager.removeFromCollection;
import static com.jaqxues.modulepackcompilerui.PreferenceManager.togglePref;
import static com.jaqxues.modulepackcompilerui.PreferencesDef.ATTRIBUTES;
import static com.jaqxues.modulepackcompilerui.PreferencesDef.PROJECT_ROOT;
import static com.jaqxues.modulepackcompilerui.PreferencesDef.SIGN_PACK;

public class Controller {

    @FXML private TreeView<File> fileTreeView;
    @FXML private TableView<List<String>> attrTable;
    @FXML private TableColumn<List<String>, String> attrNameCol;
    @FXML private TableColumn<List<String>, String> attrValueCol;
    @FXML private CheckBox toggleSignPack;
    @FXML private ProgressBar progressBar;

    public void initialize() {
        fileTreeView.setRoot(createRootItem(new File((String) getPref(PROJECT_ROOT))));
        fileTreeView.setCellFactory((param ->
                new TreeCell<File>() {
                    @Override
                    protected void updateItem(File item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null)
                            setText(item.getName());
                        else
                            setText("");
                    }
                }
        ));

        initAttributes();
        initSigning();
    }

    public void initAttributes() {
        attrNameCol.setCellValueFactory((value) ->
                new SimpleStringProperty(value.getValue().get(0)));
        attrValueCol.setCellValueFactory((value) ->
                new SimpleStringProperty(value.getValue().get(1)));

        List<List<String>> lists = getPref(ATTRIBUTES);
        for (List<String> list : lists)
            attrTable.getItems().add(list);
    }

    public void initSigning() {
        toggleSignPack.setSelected(getPref(SIGN_PACK));
    }

    private static TreeItem<File> createRootItem(File file) {
        TreeItem<File> item = new TreeItem<>(file);
        File[] children = file.listFiles();
        if (children != null)
            for (File child : children)
                item.getChildren().add(createRootItem(child));
        return item;
    }

    private void attributeInputDialog(@Nullable String originalName, @Nullable String originalValue) {
        boolean edit = originalName != null && originalValue != null;
        Dialog<List<String>> dialog = new Dialog<>();
        if (edit) {
            dialog.setTitle("Edit Attribute");
            dialog.setHeaderText("Edit the Attribute Values");
        } else {
            dialog.setTitle("Add Attribute");
            dialog.setHeaderText("Please enter a new Attribute");
        }

        dialog.getDialogPane().getButtonTypes()
                .addAll(
                        ButtonType.APPLY,
                        ButtonType.CANCEL
                );
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField name = new TextField();
        name.setPromptText("Name");
        if (edit)
            name.setText(originalName);
        TextField value = new TextField();
        value.setPromptText("Value");
        if (edit)
            value.setText(originalValue);

        grid.add(new Label("Name: "), 0, 0);
        grid.add(name, 1, 0);
        grid.add(new Label("Value: "), 0, 1);
        grid.add(value, 1, 1);

        Node applyButton = dialog.getDialogPane().lookupButton(ButtonType.APPLY);
        applyButton.setDisable(true);

        name.textProperty().addListener((observable, oldValue, newValue) ->
                applyButton.setDisable(newValue.trim().isEmpty()));

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(name::requestFocus);

        dialog.setResultConverter(param -> {
            if (param == ButtonType.APPLY && !name.getText().trim().isEmpty() && !value.getText().trim().isEmpty()) {
                List<String> strings = new ArrayList<>();
                strings.add(name.getText());
                strings.add(value.getText());
                return strings;
            }
            return null;
        });

        Optional<List<String>> result = dialog.showAndWait();

        result.ifPresent(strings -> {
            PreferenceManager.addToCollection(ATTRIBUTES, strings);
            attrTable.getItems().add(strings);
        });
    }


    // ============================================================================================
    // EVENT METHODS
    // ============================================================================================

    public void setModulePackage(ActionEvent event) {
        TreeItem<File> selectedItem = fileTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Please Select a Folder to set the Package Root");
            alert.show();
            return;
        }
        putPref(PreferencesDef.MODULE_PACKAGE, selectedItem.getValue().getAbsolutePath());
    }

    public void setProjectRoot(ActionEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        File selectedDirectory = chooser.showDialog(Main.getStage());
        if (selectedDirectory != null) {
            putPref(PreferencesDef.PROJECT_ROOT, selectedDirectory.getAbsolutePath());
            fileTreeView.setRoot(createRootItem(selectedDirectory));
            return;
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText("Please Select a Folder to set the Package Root");
        alert.show();
    }

    public void addAttribute(ActionEvent event) {
        attributeInputDialog(null, null);
    }

    public void editAttribute(ActionEvent event) {
        List<String> strings = attrTable.getSelectionModel().getSelectedItem();
        if (strings == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Please select an attribute to edit it");
            alert.show();
            return;
        }
        attrTable.getItems().remove(strings);
        removeFromCollection(ATTRIBUTES, strings);

        attributeInputDialog(strings.get(0), strings.get(1));
    }

    public void removeAttribute(ActionEvent event) {
        List<String> attributes = attrTable.getSelectionModel().getSelectedItem();
        if (attributes == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Please select an Attribute to delete it");
            alert.show();
            return;
        }
        attrTable.getItems().remove(attributes);
        removeFromCollection(ATTRIBUTES, attributes);
    }

    public void toggleSignPack(ActionEvent event) {
        togglePref(SIGN_PACK);
    }

    public void addKeyConfig(ActionEvent event) {
        // TODO com.jaqxues.modulepackcompilerui.PreferenceManager
    }

    public void editKeyConfig(ActionEvent event) {
        List<String> selected = attrTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Please Select an Attribute in the List to Edit it");
            alert.show();
            return;
        }
        attrTable.getItems().remove(selected);
        removeFromCollection(ATTRIBUTES, selected);
        attributeInputDialog(selected.get(0), selected.get(1));
    }

    public void removeKeyConfig(ActionEvent event) {
        //TODO com.jaqxues.modulepackcompilerui.PreferenceManager
    }

    public void compileModPack(ActionEvent event) {
        //TODO Add Module Pack Compiling Thing
        //TODO Add ProgressBar Implementation
    }

    public void openGeneralSettings(ActionEvent event) {
        //TODO Add General Settings
    }
}
