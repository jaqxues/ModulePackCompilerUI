package com.jaqxues.modulepackcompilerui;

import java.io.File;
import java.util.Observable;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class Controller {

    @FXML private TreeView<File> fileTreeView;

    @FXML private TableView<String[]> attrTable;
    @FXML private TableColumn<String[], String> attrNameCol;
    @FXML private TableColumn<String[], String> attrValueCol;
    @FXML private ProgressBar progressBar;

    public void initialize() {
        fileTreeView.setRoot(createRootItem(new File(".")));
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


        attrNameCol.setCellValueFactory((value) ->
                new SimpleStringProperty(value.getValue()[0]));
        attrValueCol.setCellValueFactory((value) ->
                new SimpleStringProperty(value.getValue()[1]));
    }

    private static TreeItem<File> createRootItem(File file) {
        TreeItem<File> item = new TreeItem<>(file);
        File[] children = file.listFiles();
        if (children != null)
            for (File child : children)
                item.getChildren().add(createRootItem(child));
        return item;
    }

    public void setPackage(ActionEvent event) {
        TreeItem<File> selectedItems = fileTreeView.getSelectionModel().getSelectedItem();
        if (selectedItems == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Please Select a Folder to set the Package Root");
            alert.show();
            return;
        }
        // TODO Preferences
    }

    public void addAttribute(ActionEvent event) {
        // TODO Add Input Dialog
    }

    public void editAttribute(ActionEvent event) {
        // TODO Add Input Dialog
    }

    public void removeAttribute(ActionEvent event) {
        String[] attributes = attrTable.getSelectionModel().getSelectedItem();
        if (attributes == null)
            return;
        // TODO Preferences
    }

    public void toggleSignPack(ActionEvent event) {
        // TODO Preferences
    }

    public void addKeyConfig(ActionEvent event) {
        // TODO Preferences
    }

    public void editKeyConfig(ActionEvent event) {
        //TODO Preferences
    }

    public void removeKeyConfig(ActionEvent event) {
        //TODO Preferences
    }

    public void compileModPack(ActionEvent event) {
        //TODO Add Module Pack Compiling Thing
        //TODO Add ProgressBar Implementation
    }

    public void openGeneralSettings(ActionEvent event) {
        //TODO Add General Settings
    }
}
