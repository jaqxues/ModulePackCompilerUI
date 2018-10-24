package com.jaqxues.modulepackcompilerui;

import com.jaqxues.modulepackcompilerui.models.SavedConfigModel;
import com.jaqxues.modulepackcompilerui.models.SignConfig;
import com.jaqxues.modulepackcompilerui.preferences.PreferenceManager;
import com.jaqxues.modulepackcompilerui.preferences.PreferencesDef;
import com.jaqxues.modulepackcompilerui.utils.LogUtils;
import com.jaqxues.modulepackcompilerui.utils.MiscUtils;
import com.jaqxues.modulepackcompilerui.utils.PackCompiler;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import static com.jaqxues.modulepackcompilerui.preferences.PreferenceManager.addToCollection;
import static com.jaqxues.modulepackcompilerui.preferences.PreferenceManager.clearCollection;
import static com.jaqxues.modulepackcompilerui.preferences.PreferenceManager.getPref;
import static com.jaqxues.modulepackcompilerui.preferences.PreferenceManager.putPref;
import static com.jaqxues.modulepackcompilerui.preferences.PreferenceManager.removeFromCollection;
import static com.jaqxues.modulepackcompilerui.preferences.PreferenceManager.togglePref;
import static com.jaqxues.modulepackcompilerui.preferences.PreferencesDef.ADB_PUSH_PATH;
import static com.jaqxues.modulepackcompilerui.preferences.PreferencesDef.ADB_PUSH_TOGGLE;
import static com.jaqxues.modulepackcompilerui.preferences.PreferencesDef.ATTRIBUTES;
import static com.jaqxues.modulepackcompilerui.preferences.PreferencesDef.FILE_SOURCES;
import static com.jaqxues.modulepackcompilerui.preferences.PreferencesDef.JDK_INSTALLATION_PATH;
import static com.jaqxues.modulepackcompilerui.preferences.PreferencesDef.MODULE_PACKAGE;
import static com.jaqxues.modulepackcompilerui.preferences.PreferencesDef.PROJECT_ROOT;
import static com.jaqxues.modulepackcompilerui.preferences.PreferencesDef.SDK_BUILD_TOOLS;
import static com.jaqxues.modulepackcompilerui.preferences.PreferencesDef.SIGN_CONFIGS;
import static com.jaqxues.modulepackcompilerui.preferences.PreferencesDef.SIGN_PACK;

/**
 * This file was created by Jacques (jaqxues) in the Project ModulePackCompilerUI.<br>
 * Date: 2.10.2018 - Time 18:49.
 */

public class Controller {

    @FXML
    private TableView<String> attrTable;
    @FXML
    private TableColumn<String, String> attrNameCol;
    @FXML
    private TableColumn<String, String> attrValueCol;
    @FXML
    private Button attrEditBtn;
    @FXML
    private Button attrRemoveBtn;

    @FXML
    private CheckBox toggleSignPack;
    @FXML
    private TableView<SignConfig> keyTable;
    @FXML
    private TableColumn<SignConfig, String> storePathCol;
    @FXML
    private TableColumn<SignConfig, String> storePasswordCol;
    @FXML
    private TableColumn<SignConfig, String> keyAliasCol;
    @FXML
    private TableColumn<SignConfig, String> keyPasswordCol;
    @FXML
    private Button keyEditConfigBtn;
    @FXML
    private Button keyRemoveConfigBtn;
    @FXML
    private Button selectSignConfigBtn;

    @FXML
    private TableView<SavedConfigModel> savedConfigTable;
    @FXML
    private TableColumn<SavedConfigModel, String> savedConfigNameCol;
    @FXML
    private TableColumn<SavedConfigModel, String> savedConfigNoticesCol;
    @FXML
    private TableColumn<SavedConfigModel, String> savedConfigDateCol;
    @FXML
    private Button savedEditBtn;
    @FXML
    private Button savedRemoveBtn;
    @FXML
    private Button savedRestoreBtn;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private CheckBox adbPushToggle;
    @FXML
    private Button adbPushSettings;

    // ============================================================================================
    // INITIALIZATION METHODS
    // ============================================================================================

    public void initialize() {
        initAttributes();
        initSigning();
        initSavedConfig();
        initAdbPush();
    }

    private void initAttributes() {
        attrNameCol.setCellValueFactory((value) ->
                new SimpleStringProperty(value.getValue().split("=")[0]));
        attrValueCol.setCellValueFactory((value) ->
                new SimpleStringProperty(value.getValue().split("=")[1]));

        //noinspection unchecked
        attrTable.getItems().addAll((List<String>) getPref(ATTRIBUTES));

        attrTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            attrEditBtn.setDisable(false);
            attrRemoveBtn.setDisable(false);
        });
    }

    private void initSigning() {
        boolean selected = getPref(SIGN_PACK);
        toggleSignPack.setSelected(selected);

        keyTable.setDisable(!selected);

        storePathCol.setCellValueFactory((value) ->
                new SimpleStringProperty(value.getValue().getStorePath())
        );
        storePasswordCol.setCellValueFactory((value) ->
                new SimpleStringProperty(value.getValue().getStorePassword())
        );
        keyAliasCol.setCellValueFactory((value) ->
                new SimpleStringProperty(value.getValue().getKeyAlias())
        );
        keyPasswordCol.setCellValueFactory((value) ->
                new SimpleStringProperty(value.getValue().getKeyPassword())
        );

        keyTable.setRowFactory(tv -> new TableRow<SignConfig>() {
            @Override
            protected void updateItem(SignConfig item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && item.isActivated()) {
                    setStyle("-fx-background-color: lightgreen");
                    selectedProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue)
                            setStyle("-fx-background-color: green;");
                        else
                            setStyle("-fx-background-color: lightgreen;");
                    });
                }
            }
        });

        List<SignConfig> configs = getPref(SIGN_CONFIGS);
        keyTable.getItems().addAll(configs);

        int[] selectedConfigs = {0};
        configs.forEach(signConfig -> {
            if (signConfig.isActivated())
                selectedConfigs[0]++;
        });
        if (selectedConfigs[0] > 1) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Signing Configuration");
            alert.setHeaderText("Selected more than one Signing Configuration");
            alert.setContentText("Resetting activated Signing Configurations to avoid Conflicts");
            alert.show();
            configs.forEach(signConfig -> signConfig.setActive(false));
            keyTable.refresh();
        }

        selectSignConfigBtn.setText(
                keyTable.getSelectionModel().getSelectedItem() == null || keyTable.getSelectionModel().getSelectedItem().isActivated() ?
                        "Activate" :
                        "Disable"
        );

        keyTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            selectSignConfigBtn.setDisable(false);
            selectSignConfigBtn.setText(
                    newValue.isActivated() ?
                            "Disable" :
                            "Activate"
            );
            keyEditConfigBtn.setDisable(false);
            keyRemoveConfigBtn.setDisable(false);
        });
    }

    private void initSavedConfig() {
        savedConfigNameCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getSavedConfigName()));
        savedConfigNoticesCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getSavedConfigNotices()));
        savedConfigDateCol.setCellValueFactory(param -> {
            SimpleDateFormat dateFormat = (SimpleDateFormat) DateFormat.getDateTimeInstance();
            return new SimpleStringProperty(dateFormat.format(new Date(param.getValue().getSavedConfigDate())));
        });

        savedConfigTable.getItems().addAll(
                SavedConfigModel.getConfigs()
        );

        savedConfigTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            savedEditBtn.setDisable(false);
            savedRemoveBtn.setDisable(false);
            savedRestoreBtn.setDisable(false);
        });
    }

    private void initAdbPush() {
        boolean adbPush = getPref(ADB_PUSH_TOGGLE);
        adbPushToggle.setSelected(adbPush);
        adbPushSettings.setDisable(!adbPush);
        // TODO JADB IMPLEMENTATION
    }

    // ============================================================================================
    // DIALOGS AND SIMILAR UI COMPONENTS
    // ============================================================================================

    private void attrInputDialog(@Nullable String string) {
        boolean edit = string != null;
        Dialog<String> dialog = new Dialog<>();
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
        TextField value = new TextField();
        value.setPromptText("Value");
        if (edit) {
            name.setText(string.split("=", 2)[0]);
            value.setText(string.split("=", 2)[1]);
        }

        grid.add(new Label("Name: "), 0, 0);
        grid.add(name, 1, 0);
        grid.add(new Label("Value: "), 0, 1);
        grid.add(value, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(name::requestFocus);

        dialog.setResultConverter(param -> {
            if (param == ButtonType.APPLY
                    && !name.getText().trim().isEmpty()
                    && !value.getText().trim().isEmpty()) {
                return name.getText().trim() + "=" + value.getText().trim();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(string1 -> {
            if (edit) {
                attrTable.getItems().remove(string);
                removeFromCollection(ATTRIBUTES, string);
            }
            addToCollection(ATTRIBUTES, string1);
            attrTable.getItems().add(string1);
        });
    }

    private void keyInputDialog(@Nullable SignConfig oldConfig) {
        boolean edit = oldConfig != null;

        Dialog<SignConfig> dialog = new Dialog<>();
        if (edit) {
            dialog.setTitle("Edit Key Configuration");
            dialog.setHeaderText("Edit the Keys configuration");
        } else {
            dialog.setTitle("Add Key Configuration");
            dialog.setHeaderText("Please enter a new Key Configuration");
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

        TextField storePath = new TextField();
        storePath.setPromptText("KeyStore Path");
        TextField storePassword = new TextField();
        storePassword.setPromptText("KeyStore Password");
        TextField keyAlias = new TextField();
        keyAlias.setPromptText("Key Alias");
        TextField keyPassword = new TextField();
        keyPassword.setPromptText("Key Password");
        if (edit) {
            storePath.setText(oldConfig.getStorePath());
            storePassword.setText(oldConfig.getStorePassword());
            keyAlias.setText(oldConfig.getKeyAlias());
            keyPassword.setText(oldConfig.getKeyPassword());
        }

        Button storeButtonChooser = new Button("Browse...");
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("KeyStore Files", "*.jks")
        );
        storeButtonChooser.setOnAction(event -> {
            File file = fileChooser.showOpenDialog(Main.getStage());
            if (file != null)
                storePath.setText(file.getAbsolutePath());
        });

        grid.add(new Label("KeyStore Path: "), 0, 0);
        grid.add(storePath, 1, 0);
        grid.add(storeButtonChooser, 2, 0);
        grid.add(new Label("KeyStore Password"), 0, 1);
        grid.add(storePassword, 1, 1);
        grid.add(new Label("KeyAlias"), 0, 2);
        grid.add(keyAlias, 1, 2);
        grid.add(new Label("Key Password"), 0, 3);
        grid.add(keyPassword, 1, 3);

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(storePath::requestFocus);

        dialog.setResultConverter(param -> {
            if (param == ButtonType.APPLY
                    && !storePath.getText().trim().isEmpty()
                    && !storePassword.getText().trim().isEmpty()
                    && !keyAlias.getText().trim().isEmpty()
                    && !keyPassword.getText().trim().isEmpty()) {
                return new SignConfig(
                        storePath.getText().trim(),
                        storePassword.getText().trim(),
                        keyAlias.getText().trim(),
                        keyPassword.getText().trim());
            }
            return null;
        });

        Optional<SignConfig> result = dialog.showAndWait();

        result.ifPresent(signConfig -> {
            if (!new File(signConfig.getStorePath()).exists()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Sign Configuration");
                alert.setHeaderText("Keystore does not exist");
                alert.setContentText("This Keystore does not exist, please create a Keystore or correct the Path");
                alert.show();
                return;
            }
            if (edit) {
                keyTable.getItems().remove(oldConfig);
                removeFromCollection(SIGN_CONFIGS, oldConfig);
            }
            addToCollection(SIGN_CONFIGS, signConfig);
            keyTable.getItems().add(signConfig);
        });
    }

    private void savedConfigInputDialog(@Nullable SavedConfigModel savedConfigModel) {
        boolean edit = savedConfigModel != null;
        Dialog<SavedConfigModel> dialog = new Dialog<>();
        dialog.setTitle("Saved Configurations");
        if (edit)
            dialog.setHeaderText("Edit Name And Notices");
        else
            dialog.setHeaderText("Enter A Name And a Notice");

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
        TextField notice = new TextField();
        notice.setPromptText("Notice");
        if (edit) {
            name.setText(savedConfigModel.getSavedConfigName());
            notice.setText(savedConfigModel.getSavedConfigNotices());
        }
        notice.setPrefWidth(300d);
        name.setPrefWidth(300d);

        grid.add(new Label("Name: "), 0, 0);
        grid.add(name, 1, 0);
        grid.add(new Label("Notice: "), 0, 1);
        grid.add(notice, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(name::requestFocus);

        dialog.setResultConverter(param -> {
            if (param == ButtonType.CANCEL)
                return null;
            if (!name.getText().trim().isEmpty()) {
                if (edit)
                    return savedConfigModel.setSavedConfigName(name.getText().trim())
                            .setSavedConfigNotices(name.getText().trim())
                            .setSavedConfigDate(System.currentTimeMillis());
                SignConfig[] signConfigs = new SignConfig[1];
                keyTable.getItems().forEach(signConfig -> {
                    if (signConfig.isActivated())
                        signConfigs[0] = signConfig;
                });
                // TOdo disabled
                return new SavedConfigModel().setSavedConfigName(name.getText().trim())
                        .setSavedConfigNotices(name.getText().trim())
                        .setSavedConfigDate(System.currentTimeMillis())
                        .setProjectRoot(getPref(PROJECT_ROOT))
                        .setModulePackage(getPref(MODULE_PACKAGE))
                        .setModuleSources(getPref(FILE_SOURCES))
                        .setAttributes(attrTable.getItems())
                        .setSignConfig(signConfigs[0]);
            }
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Empty Name");
            alert.setHeaderText("Empty Name");
            alert.setContentText("You cannot provide empty names to save a Configuration");
            alert.show();
            return null;
        });

        Optional<SavedConfigModel> result = dialog.showAndWait();

        result.ifPresent(savedConfigModel1 -> {
            if (edit) {
                savedConfigTable.getItems().remove(savedConfigModel);
                SavedConfigModel.removeConfig(savedConfigModel);
            }
            if (!SavedConfigModel.addConfig(savedConfigModel1)) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Duplicate Detected");
                alert.setTitle("Unable to save Configuration");
                alert.setContentText("A Saved Configuration with the same Name already exists, please chose a new name or delete the old Configuration.");
                alert.show();
            } else
                savedConfigTable.getItems().add(savedConfigModel1);
        });
    }

    @CheckReturnValue
    private Dialog<String> getDirSelectorDialog(String title, String header, String contentText,
                                                String pathName, String pathValue) {
        return getDirSelectorDialog(title, header, contentText, pathName, pathValue, null);
    }

    @CheckReturnValue
    private Dialog<String> getDirSelectorDialog(String title, String header, String contentText,
                                                String pathName, @Nullable String pathValue, Alert alert) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);

        dialog.getDialogPane().getButtonTypes().addAll(
                ButtonType.APPLY,
                ButtonType.CANCEL
        );

        TextField textInput = new TextField();
        textInput.setPrefWidth(500d);
        textInput.setPromptText(pathName);
        if (pathValue != null)
            textInput.setText(pathValue);
        Button pathBtn = new Button("Browse...");
        pathBtn.setOnAction((value) -> {
            if (alert != null) {
                alert.showAndWait().ifPresent(buttonType -> {
                    if (buttonType == ButtonType.OK) {
                        File file = getDirChooser(title, pathValue).showDialog(Main.getStage());
                        if (file != null)
                            textInput.setText(file.getAbsolutePath());
                    }
                });
            } else {
                File file = getDirChooser(title, pathValue).showDialog(Main.getStage());
                if (file != null)
                    textInput.setText(file.getAbsolutePath());
            }
        });


        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(30, 10, 30, 10));
        gridPane.setHgap(10d);
        gridPane.setVgap(10d);
        gridPane.add(new Label(pathName), 0, 0);
        gridPane.add(textInput, 1, 0);
        gridPane.add(pathBtn, 2, 0);
        dialog.getDialogPane().setContent(gridPane);

        Platform.runLater(textInput::requestFocus);

        if (contentText != null) {
            Label textContent = new Label(contentText);
            gridPane.add(textContent, 0, 1, 3, 1);
        }

        dialog.setResultConverter(param -> {
            if (param == ButtonType.APPLY && !textInput.getText().trim().isEmpty()) {
                File file = new File(textInput.getText().trim());
                if (!file.exists()) {
                    Alert alert1 = new Alert(Alert.AlertType.ERROR);
                    alert1.setTitle(title);
                    alert.setHeaderText("This Path does not exist");
                    alert.setContentText("The file or folder you specified does not exist");
                    alert.show();
                    return null;
                }
                return file.getAbsolutePath();
            }
            return null;
        });
        return dialog;
    }

    @CheckReturnValue
    private static DirectoryChooser getDirChooser(String title, @Nullable String initial) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(title);
        if (initial != null) {
            File initialDir = new File(initial);
            if (initialDir.exists())
                chooser.setInitialDirectory(initialDir);
        }
        return chooser;
    }

    // ============================================================================================
    // EVENT METHODS
    // ============================================================================================

    public void setModulePackage(ActionEvent event) {
        TextInputDialog inputDialog = new TextInputDialog();
        inputDialog.setTitle("General Settings");
        inputDialog.setHeaderText("Set Module Package");
        inputDialog.setContentText("The Module Package Name is used to determine which files and code will be included in the Pack.\nAll Files in this Java Package will be included (and SubFiles of course).\n\nSnapTools uses for example \"com.ljmu.andre.snaptools.ModulePack\"\n\nIn case this PackageName is wrong, your pack will either not be compiled or won't work at all.");
        inputDialog.getEditor().setPromptText("com.ljmu.andre.snaptools.ModulePack");
        inputDialog.getEditor().setText(getPref(MODULE_PACKAGE));
        inputDialog.getEditor().setPrefWidth(300d);
        inputDialog.showAndWait().ifPresent(
                s -> putPref(MODULE_PACKAGE, s)
        );
    }

    public void setProjectRoot(ActionEvent event) {
        getDirSelectorDialog(
                "General Settings",
                "Set Project Root",
                "You can specify a new Project Root",
                "Project Root",
                getPref(PROJECT_ROOT)
        ).showAndWait().ifPresent(s -> putPref(PROJECT_ROOT, s)
        );
    }

    public void setSources(ActionEvent event) {
        String projectRoot = getPref(PROJECT_ROOT);
        if (projectRoot == null || !new File(projectRoot).exists()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Manual Project Sources");
            alert.setHeaderText("No Project Root set");
            alert.setContentText("The Project Root needs to be set in order to set the Module Package");
            alert.show();
            return;
        }
        File projectRootFile = new File(projectRoot);

        Dialog<List<String>> dialog = new Dialog<>();
        dialog.setTitle("Manual Project Sources");
        dialog.setHeaderText("Manually set project sources");

        dialog.getDialogPane().getButtonTypes()
                .addAll(ButtonType.APPLY,
                        ButtonType.CANCEL
                );

        ListView<String> listView = new ListView<>();
        //noinspection unchecked
        listView.getItems().addAll((List<String>) getPref(FILE_SOURCES));
        listView.setPadding(new Insets(10));

        ButtonBar buttonBar = new ButtonBar();
        buttonBar.getButtons().addAll(
                new Button("New..."),
                new Button("Edit"),
                new Button("Remove")
        );
        buttonBar.setPadding(new Insets(10));
        buttonBar.getButtons().get(1).setDisable(true);
        buttonBar.getButtons().get(2).setDisable(true);
        buttonBar.getButtons().get(0).addEventHandler(ActionEvent.ANY, event1 -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Manual Project Sources");
            directoryChooser.setInitialDirectory(projectRootFile);
            File selected = directoryChooser.showDialog(Main.getStage());
            if (selected == null) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("Please select a Folder to use the Manual Project Sources Feature");
                alert.setHeaderText("No Folder Selected");
                alert.setTitle("Manual Project Sources");
                alert.show();
                return;
            }

            String relativized = File.separator + projectRootFile.toPath().relativize(selected.toPath()).toString() + File.separator;
            LogUtils.getLogger().debug("Relativized Path: " + relativized);
            addToCollection(FILE_SOURCES, relativized);
            listView.getItems().add(relativized);
        });
        buttonBar.getButtons().get(1).addEventHandler(ActionEvent.ANY, event12 -> {
            String selected = listView.getSelectionModel().getSelectedItem();

            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setInitialDirectory(new File(projectRootFile, selected));
            chooser.setTitle("Manual Project Sources");
            File chosenDir = chooser.showDialog(Main.getStage());
            if (chosenDir == null) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("Please select a Folder to edit a source.");
                alert.setHeaderText("No Folder Selected");
                alert.setTitle("Manual Project Sources");
                alert.show();
                return;
            }

            listView.getItems().remove(selected);
            removeFromCollection(FILE_SOURCES, selected);

            String relativized = File.separator + projectRootFile.toPath().relativize(chosenDir.toPath()).toString() + File.separator;
            LogUtils.getLogger().debug("Relativized Path: " + relativized);
            addToCollection(FILE_SOURCES, relativized);
            listView.getItems().add(relativized);
        });
        buttonBar.getButtons().get(2).addEventHandler(ActionEvent.ANY, event13 -> {
            String selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Manual Project Sources");
                alert.setHeaderText("No Selected an Item");
                alert.setContentText("Please select an item in the list to delete it.");
                alert.show();
                return;
            }
            listView.getItems().remove(selected);
            removeFromCollection(FILE_SOURCES, selected);
        });

        listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            buttonBar.getButtons().get(1).setDisable(false);
            buttonBar.getButtons().get(2).setDisable(false);
        });

        VBox vBox = new VBox(listView, buttonBar);
        vBox.setFillWidth(true);
        vBox.setPrefWidth(600d);
        dialog.getDialogPane().setContent(vBox);
        dialog.showAndWait();
    }

    public void addAttribute(ActionEvent event) {
        attrInputDialog(null);
    }

    public void editAttribute(ActionEvent event) {
        attrInputDialog(attrTable.getSelectionModel().getSelectedItem());
    }

    public void removeAttribute(ActionEvent event) {
        String attribute = attrTable.getSelectionModel().getSelectedItem();
        attrTable.getItems().remove(attribute);
        removeFromCollection(ATTRIBUTES, attribute);
    }

    public void toggleSignPack(ActionEvent event) {
        keyTable.setDisable(
                !togglePref(SIGN_PACK)
        );
    }

    public void addKeyConfig(ActionEvent event) {
        keyInputDialog(null);
    }

    public void editKeyConfig(ActionEvent event) {
        keyInputDialog(keyTable.getSelectionModel().getSelectedItem());
    }

    public void removeKeyConfig(ActionEvent event) {
        keyTable.getItems().remove(keyTable.getSelectionModel().getSelectedItem());
    }

    public void compileModPack(ActionEvent event) {
        // TODO
        String preferenceCheck = checkPreferences();
        if (preferenceCheck != null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(preferenceCheck);
            alert.show();
            return;
        }

        List<File> sources = new ArrayList<>();
//                        + (debug ? "/app/build/intermediates/transforms/desugar/pack/debug/0/" : "/app/build/intermediates/transforms/desugar/pack/release/0/")
//                        + getPref(MODULE_PACKAGE)
//                                + "/app/build/intermediates/transforms/desugar/pack/release/0/"
//                        + (debug ? "/app/build/tmp/kotlin-classes/packDebug/" : "/app/build/tmp/kotlin-classes/packRelease/")
//                        + getPref(MODULE_PACKAGE)
//                                + "/app/build/tmp/kotlin-classes/packRelease/"

        //noinspection unchecked
        for (String string : (ArrayList<String>) getPref(FILE_SOURCES)) {
            sources.add(new File(getPref(PROJECT_ROOT) + string + MiscUtils.getMPFolder()));
        }

        try {
            progressBar.setVisible(true);
            progressBar.setProgress(-1.0);
            PackCompiler packCompiler = new PackCompiler.Builder()
                    .setAttributes(attrTable.getItems())
                    .setJarTarget(new File("Files/Packs/STModulePack")) // TODO Use File From Template
                    .setSignConfig(keyTable.getSelectionModel().getSelectedItem())
                    .setSources(sources)
                    .build();
            // TODO Async
            LogUtils.getLogger().debug("Built PackCompiler Instance, Executing task...");
            packCompiler.call();

            LogUtils.getLogger().debug("Finished Pack Compiler");
        } catch (Exception e) {
            LogUtils.getLogger().error("Could not compile Pack", e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.show();
        }
        progressBar.setVisible(false);
        progressBar.setProgress(0);
    }

    public void setSDKBuildTools(ActionEvent event) {
        String path = getPref(SDK_BUILD_TOOLS);
        if (path == null || !new File(path).exists()) {
            path = MiscUtils.getLocalAppDataDir() + "/Android/Sdk/build-tools";
            if (new File(path).exists())
                path = MiscUtils.getLocalAppDataDir();
        }

        getDirSelectorDialog(
                "Path Configurations",
                "SDK Build Tools Installation Path",
                "Example Folder: C:\\Users\\ExampleUser\\AppData\\Local\\Android\\Sdk\\build-tools\\26.0.2\n\n The Build Tools Version does not matter. Please make sure that the Folder contains the \"dx.bat\" File which is an essential part of compiling Module Packs.\n\nIf the Folder does not contain a such file, please check in the SDK Manager and download the BuildTools.",
                "Build Tools Path",
                path
        ).showAndWait().ifPresent(s -> putPref(SDK_BUILD_TOOLS, s));
    }

    public void setJDKInstallation(ActionEvent event) {
        String path = getPref(JDK_INSTALLATION_PATH);
        if (path == null || !new File(path).exists()) {
            path = MiscUtils.getProgramFilesDir() + "/Java";
            if (!new File(path).exists())
                path = MiscUtils.getProgramFilesDir();
        }

        getDirSelectorDialog(
                "Path Configurations",
                "JDK Installation Path",
                "Example Folder: C:\\Program Files\\Java\\jdk1.8.0_172\n\nPlease use JDK 1.8.0 for Android Development. This Path will be used to find the jarsigner.exe in /bin of this Folder.",
                "JDK Installation Path",
                path
        ).showAndWait().ifPresent(s -> putPref(JDK_INSTALLATION_PATH, s));
    }

    public void saveSavedConfig(ActionEvent event) {
        savedConfigInputDialog(null);
    }

    public void editSavedConfig(ActionEvent event) {
        savedConfigInputDialog(savedConfigTable.getSelectionModel().getSelectedItem());
    }

    public void restoreSavedConfig(ActionEvent event) {
        SavedConfigModel model = savedConfigTable.getSelectionModel().getSelectedItem();

        if (model.getProjectRoot() == null || !new File(model.getProjectRoot()).exists()) {
            getDirSelectorDialog(
                    "Saved Configurations",
                    "Choose Project Root",
                    "This Saved Config does not have a valid Project Root. Please specify a new Directory.",
                    "Project Root",
                    null
            ).showAndWait().ifPresent(s -> {
                model.setProjectRoot(s);
                putPref(PROJECT_ROOT, model.setProjectRoot(s));
            });
        }
        for (String string : PreferenceManager.<List<String>>getPref(ATTRIBUTES))
            attrTable.getItems().remove(string);
        for (String string : putPref(ATTRIBUTES, model.getAttributes()))
            attrTable.getItems().add(string);

        putPref(PROJECT_ROOT, model.getProjectRoot());
        putPref(MODULE_PACKAGE, model.getModulePackage());

        toggleSignPack.setSelected(
                putPref(SIGN_PACK, model.getSignConfig() != null)
        );

        if (model.getSignConfig() != null) {
            if (!keyTable.getItems().contains(model.getSignConfig()))
                keyTable.getItems().add(model.getSignConfig());
            model.getSignConfig().setActive(true);
            keyTable.getSelectionModel().select(model.getSignConfig());
        }

        putPref(FILE_SOURCES, model.getModuleSources());
    }

    public void removeSavedConfig(ActionEvent event) {
        SavedConfigModel model = savedConfigTable.getSelectionModel().getSelectedItem();
        savedConfigTable.getItems().remove(model);
        SavedConfigModel.removeConfig(model);
    }

    public void resetCurrentPrefs(ActionEvent event) {
        attrTable.getItems().clear();
        clearCollection(ATTRIBUTES);
        // TODO Finish Resetting Current Config
    }

    public void adbPushSettings(ActionEvent event) {
        // TODO Add Dialog if multiple Devices etc
        TextInputDialog inputDialog = new TextInputDialog();
        inputDialog.setTitle("ADB Push Path");
        inputDialog.setHeaderText("Set ADB Push Path for your phone");
        if (getPref(ADB_PUSH_PATH) != null)
            inputDialog.getEditor().setText(getPref(ADB_PUSH_PATH));
        inputDialog.getEditor().setPrefWidth(400d);
        inputDialog.getEditor().setPromptText("ADB Push Path");
        inputDialog.setContentText("The Adb Push Path defines where the Packs are pushed to on your phone.\nUsually /sdcard/SnapTools/ModulePacks/");
        inputDialog.showAndWait().ifPresent(s -> putPref(ADB_PUSH_PATH, s));
    }

    public void adbPushToggle(ActionEvent event) {
        adbPushSettings.setDisable(
                !togglePref(ADB_PUSH_TOGGLE)
        );
    }

    public void activateSignConfig(ActionEvent event) {
        keyTable.getItems().forEach(signConfig -> signConfig.setActive(false));
        keyTable.getSelectionModel().getSelectedItem().setActive(true);
        keyTable.refresh();
    }


    // ============================================================================================
    // OTHERS
    // ============================================================================================

    private String checkPreferences() {
        if (getPref(PROJECT_ROOT) == null)
            return "Select a Project Root";
        if (getPref(MODULE_PACKAGE) == null)
            return "Select a Package to build to Module Pack from";
        if (getPref(SIGN_PACK)) {
            if ((((Collection<?>) getPref(SIGN_CONFIGS)).isEmpty()))
                return "Disable Signing Packs or add a new Key Configuration";
            int[] foundActivated = new int[]{0};
            keyTable.getItems().forEach(signConfig -> {
                if (signConfig.isActivated())
                    foundActivated[0]++;
            });
            if (foundActivated[0] != 1)
                return (foundActivated[0] == 0 ? "Unable to find an" : "Found more than one")
                        + " activated Signing Configuration. Please select only one Singing Configuration";
        }
        if (getPref(SDK_BUILD_TOOLS) == null)
            return "Set the SDK Build Tools Path in General Settings";
        if (getPref(JDK_INSTALLATION_PATH) == null)
            return "Set the JDK Installation Path in General Settings";
        return null;
    }
}
