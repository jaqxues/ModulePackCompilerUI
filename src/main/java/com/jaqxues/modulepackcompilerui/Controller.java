package com.jaqxues.modulepackcompilerui;

import com.sun.istack.internal.Nullable;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import static com.jaqxues.modulepackcompilerui.PreferenceManager.getPref;
import static com.jaqxues.modulepackcompilerui.PreferenceManager.putPref;
import static com.jaqxues.modulepackcompilerui.PreferenceManager.removeFromCollection;
import static com.jaqxues.modulepackcompilerui.PreferenceManager.togglePref;
import static com.jaqxues.modulepackcompilerui.PreferencesDef.ATTRIBUTES;
import static com.jaqxues.modulepackcompilerui.PreferencesDef.FILE_SOURCES;
import static com.jaqxues.modulepackcompilerui.PreferencesDef.JDK_INSTALLATION_PATH;
import static com.jaqxues.modulepackcompilerui.PreferencesDef.MODULE_PACKAGE;
import static com.jaqxues.modulepackcompilerui.PreferencesDef.PROJECT_ROOT;
import static com.jaqxues.modulepackcompilerui.PreferencesDef.SDK_BUILD_TOOLS;
import static com.jaqxues.modulepackcompilerui.PreferencesDef.SELECTED_SIGN_CONFIG;
import static com.jaqxues.modulepackcompilerui.PreferencesDef.SIGN_CONFIGS;
import static com.jaqxues.modulepackcompilerui.PreferencesDef.SIGN_PACK;

public class Controller {

    @FXML
    private TableView<String> attrTable;
    @FXML
    private TableColumn<String, String> attrNameCol;
    @FXML
    private TableColumn<String, String> attrValueCol;

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
    private TableView<SavedConfigModel> savedConfigTable;
    @FXML
    private TableColumn<SavedConfigModel, String> savedConfigNameCol;
    @FXML
    private TableColumn<SavedConfigModel, String> savedConfigNoticesCol;
    @FXML
    private TableColumn<SavedConfigModel, String> savedConfigDateCol;

    @FXML
    private ProgressBar progressBar;

    // ============================================================================================
    // INIT METHODS
    // ============================================================================================

    public void initialize() {
        initAttributes();
        initSigning();
        initSavedConfig();
    }

    private void initAttributes() {
        attrNameCol.setCellValueFactory((value) ->
                new SimpleStringProperty(value.getValue().split("=")[0]));
        attrValueCol.setCellValueFactory((value) ->
                new SimpleStringProperty(value.getValue().split("=")[1]));

        List<String> list = getPref(ATTRIBUTES);
        for (String item : list)
            attrTable.getItems().add(item);
    }

    private void initSigning() {
        toggleSignPack.setSelected(getPref(SIGN_PACK));

        keyTable.setDisable(!(boolean) getPref(SIGN_PACK));

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

        List<SignConfig> configs = getPref(SIGN_CONFIGS);
        if (!configs.isEmpty()) {
            for (SignConfig config : configs)
                keyTable.getItems().add(config);
        }

        SignConfig selected = getPref(SELECTED_SIGN_CONFIG);
        if (selected != null && keyTable.getItems().contains(selected))
            keyTable.getSelectionModel().select(selected);
    }

    private void initSavedConfig() {
        savedConfigNameCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getSavedConfigName()));
        savedConfigNoticesCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getSavedConfigNotices()));
        savedConfigDateCol.setCellValueFactory(param -> {
            SimpleDateFormat dateFormat = (SimpleDateFormat) DateFormat.getDateTimeInstance();
            return new SimpleStringProperty(dateFormat.format(new Date(param.getValue().getSavedConfigDate())));
        });

        if (SavedConfigModel.getConfigs().length == 0)
            SavedConfigModel.addConfig(
                    new SavedConfigModel()
                            .setModulePackage("com.ljmu.andre.snaptools.ModulePack")
                            .setSavedConfigDate(System.currentTimeMillis())
                            .setSavedConfigName("Default SnapTools Configuration")
                            .setAttributes(Arrays.asList(
                                    "Development=TRUE",
                                    "PackVersion=1.0.0.0",
                                    "Flavour=prod",
                                    "Type=Premium",
                                    "SCVersion=10.41.6.0"
                            ))
                            .setSavedConfigDate(System.currentTimeMillis())
                            .setSavedConfigNotices("Default Configuration for a SnapTools ModulePack")
                            .setModuleSources(Arrays.asList(
                                    "/app/build/intermediates/transforms/desugar/pack/release/0/",
                                    "/app/build/tmp/kotlin-classes/packRelease/"
                            ))
            );

        savedConfigTable.getItems().addAll(
                SavedConfigModel.getConfigs()
        );
    }

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
            PreferenceManager.addToCollection(ATTRIBUTES, string1);
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
        storeButtonChooser.setOnAction(event ->
                fileChooser.showOpenDialog(Main.getStage()));

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
            if (edit) {
                keyTable.getItems().remove(oldConfig);
                removeFromCollection(SIGN_CONFIGS, oldConfig);
            }
            PreferenceManager.addToCollection(SIGN_CONFIGS, signConfig);
            keyTable.getItems().add(signConfig);
        });
    }

    private String checkPreferences() {
        if (getPref(PROJECT_ROOT) == null)
            return "Select a Project Root";
        if (getPref(MODULE_PACKAGE) == null)
            return "Select a Package to build to Module Pack from";
        if ((boolean) getPref(SIGN_PACK) && (((Collection<?>) getPref(SIGN_CONFIGS)).isEmpty() || keyTable.getSelectionModel().getSelectedItem() == null))
            return "Disable Signing Packs or select/add a new Key Configuration";
        if (getPref(SDK_BUILD_TOOLS) == null)
            return "Set the SDK Build Tools Path in General Settings";
        if (getPref(JDK_INSTALLATION_PATH) == null)
            return "Set the JDK Installation Path in General Settings";
        return null;
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

        grid.add(new Label("Name: "), 0, 0);
        grid.add(name, 1, 0);
        grid.add(new Label("Notice: "), 0, 1);
        grid.add(notice, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(name::requestFocus);

        dialog.setResultConverter(param -> {
            if (param == ButtonType.APPLY
                    && !name.getText().trim().isEmpty()
                    && !notice.getText().trim().isEmpty()) {
                if (edit)
                    return savedConfigModel.setSavedConfigName(name.getText().trim())
                            .setSavedConfigNotices(name.getText().trim())
                            .setSavedConfigDate(System.currentTimeMillis());
                return new SavedConfigModel().setSavedConfigName(name.getText().trim())
                        .setSavedConfigNotices(name.getText().trim())
                        .setSavedConfigDate(System.currentTimeMillis())
                        .setProjectRoot(getPref(PROJECT_ROOT))
                        .setModulePackage(getPref(MODULE_PACKAGE))
                        .setModuleSources(getPref(FILE_SOURCES))
                        .setAttributes(attrTable.getItems())
                        .setSignConfig(getPref(SELECTED_SIGN_CONFIG));
            }
            return null;
        });

        Optional<SavedConfigModel> result = dialog.showAndWait();

        result.ifPresent(savedConfigModel1 -> {
            if (edit) {
                savedConfigTable.getItems().remove(savedConfigModel);
                SavedConfigModel.removeConfig(savedConfigModel);
            }
            SavedConfigModel.addConfig(savedConfigModel1);
            savedConfigTable.getItems().add(savedConfigModel1);
        });
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
        inputDialog.showAndWait().ifPresent(
                s -> putPref(MODULE_PACKAGE, s)
        );
    }

    public void setProjectRoot(ActionEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        File selectedDirectory = chooser.showDialog(Main.getStage());
        if (selectedDirectory != null) {
            putPref(PreferencesDef.PROJECT_ROOT, selectedDirectory.getAbsolutePath());
            return;
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText("Please Select a Folder to set the Package Root");
        alert.show();
    }

    public void setSources(ActionEvent event) {
        Dialog<List<File>> dialog = new Dialog<>();
        // TODO FINISH
    }

    public void addAttribute(ActionEvent event) {
        attrInputDialog(null);
    }

    public void editAttribute(ActionEvent event) {
        String attribute = attrTable.getSelectionModel().getSelectedItem();
        if (attribute == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Please select an attribute to edit it");
            alert.show();
            return;
        }

        attrInputDialog(attribute);
    }

    public void removeAttribute(ActionEvent event) {
        String attribute = attrTable.getSelectionModel().getSelectedItem();
        if (attribute == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Please select an Attribute to delete it");
            alert.show();
            return;
        }
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
        SignConfig selected = keyTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Please Select a Key in the List to Edit it");
            alert.show();
            return;
        }

        keyInputDialog(selected);
    }

    public void removeKeyConfig(ActionEvent event) {
        SignConfig selected = keyTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Please Select a Key in the List to Remove it");
            alert.show();
            return;
        }

        keyTable.getItems().remove(selected);
    }

    public void compileModPack(ActionEvent event) {
        String preferenceCheck = checkPreferences();
        if (preferenceCheck != null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(preferenceCheck);
            alert.show();
            return;
        }

        List<File> sources = Arrays.asList(
                new File(
                        getPref(PROJECT_ROOT)
//                        + (debug ? "/app/build/intermediates/transforms/desugar/pack/debug/0/" : "/app/build/intermediates/transforms/desugar/pack/release/0/")
//                        + getPref(MODULE_PACKAGE)
                                + "/app/build/intermediates/transforms/desugar/pack/release/0/"
                                + getPref(MODULE_PACKAGE)),
                new File(
                        getPref(PROJECT_ROOT)
//                        + (debug ? "/app/build/tmp/kotlin-classes/packDebug/" : "/app/build/tmp/kotlin-classes/packRelease/")
//                        + getPref(MODULE_PACKAGE)
                                + "/app/build/tmp/kotlin-classes/packRelease/"
                                + getPref(MODULE_PACKAGE))
        );

        try {
            PackCompiler packCompiler = new PackCompiler.Builder()
                    .setAttributes(attrTable.getItems())
                    .setJarTarget(new File(""))
                    .setSignConfig(keyTable.getSelectionModel().getSelectedItem())
                    .setSources(sources)
                    .build();
            packCompiler.call();

            LogUtils.getLogger().debug("Finished Pack Compiler");
        } catch (Exception e) {
            LogUtils.getLogger().error("Could not compile Pack", e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }

    public void setSDKBuildTools(ActionEvent event) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Path Configurations");
        dialog.setHeaderText("SDK Build Tools Installation Path");

        dialog.getDialogPane().getButtonTypes().addAll(
                ButtonType.APPLY,
                ButtonType.CANCEL
        );

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(0, 150, 10, 10));

        TextField buildToolsPath = new TextField();
        buildToolsPath.setPromptText("Build Tools Path");
        Button sdkButtonChooser = new Button("Browse...");
        sdkButtonChooser.setOnAction((value) -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Example Folder: C:\\Users\\ExampleUser\\AppData\\Local\\Android\\Sdk\\build-tools\\26.0.2\n\n The Build Tools Version does not matter. Please make sure that the Folder contains the \"dx.bat\" File which is an essential part of compiling Module Packs.\n\nIf the Folder does not contain a such file, please check in the SDK Manager and download the BuildTools.");
            alert.setOnCloseRequest((dialogEvent) -> {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                File file = new File(System.getenv("LOCALAPPDATA") + "/Android/Sdk/build-tools");
                if (file.exists())
                    directoryChooser.setInitialDirectory(file);
                File result = directoryChooser.showDialog(Main.getStage());
                if (result != null)
                    buildToolsPath.setText(result.getAbsolutePath());
                alert.close();
            });
            alert.show();
        });

        grid.add(new Label("SDK BuildTools Path"), 0, 0);
        grid.add(buildToolsPath, 1, 0);
        grid.add(sdkButtonChooser, 2, 0);

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(buildToolsPath::requestFocus);

        dialog.setResultConverter(param -> {
            if (param == ButtonType.APPLY
                    && !buildToolsPath.getText().trim().isEmpty()) {
                return buildToolsPath.getText().trim();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(strings -> {
            putPref(SDK_BUILD_TOOLS, result.get());
        });
    }

    public void setJDKInstallation(ActionEvent event) {
        Dialog<String> setJDKDialog = new Dialog<>();
        setJDKDialog.setTitle("Path Configurations");
        setJDKDialog.setHeaderText("JDK Installation Path");

        setJDKDialog.getDialogPane().getButtonTypes().addAll(
                ButtonType.APPLY,
                ButtonType.CANCEL
        );

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(0, 150, 10, 10));

        TextField jdkPath = new TextField();
        jdkPath.setPromptText("JDK Installation Path");
        Button jdkButtonChooser = new Button("Browse...");
        jdkButtonChooser.setOnAction((value) -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Example Folder: C:\\Program Files\\Java\\jdk1.8.0_172\n\nPlease use JDK 1.8.0 for Android Development. This Path will be used to find the jarsigner.exe in /bin of this Folder.");
            alert.setOnCloseRequest((dialogEvent) -> {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                String programFiles = System.getenv("ProgramFiles");
                if (programFiles != null && !programFiles.trim().isEmpty()) {
                    File java = new File(programFiles + "/Java");
                    if (java.exists())
                        directoryChooser.setInitialDirectory(java);
                    else
                        directoryChooser.setInitialDirectory(new File(programFiles));
                }
                File result = directoryChooser.showDialog(Main.getStage());
                if (result != null)
                    jdkPath.setText(result.getAbsolutePath());
                alert.close();
            });
            alert.show();
        });

        grid.add(new Label("JDK Installation Path"), 0, 0);
        grid.add(jdkPath, 1, 0);
        grid.add(jdkButtonChooser, 2, 0);

        setJDKDialog.getDialogPane().setContent(grid);

        Platform.runLater(jdkPath::requestFocus);

        setJDKDialog.setResultConverter(param -> {
            if (param == ButtonType.APPLY
                    && !jdkPath.getText().trim().isEmpty()) {
                return jdkPath.getText().trim();
            }
            return null;
        });

        Optional<String> result = setJDKDialog.showAndWait();

        result.ifPresent(string ->
                putPref(JDK_INSTALLATION_PATH, result.get()));
    }

    public void saveSavedConfig(ActionEvent event) {
        // TODO Check Preferences
        savedConfigInputDialog(null);
    }

    public void editSavedConfig(ActionEvent event) {
        SavedConfigModel savedConfigModel = savedConfigTable.getSelectionModel().getSelectedItem();
        if (savedConfigModel == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Please select a Saved Configuration to edit it");
            alert.show();
            return;
        }
        savedConfigInputDialog(savedConfigModel);
    }

    public void restoreSavedConfig(ActionEvent event) {
        SavedConfigModel model = savedConfigTable.getSelectionModel().getSelectedItem();
        if (model == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Please select a Configuration to restore it");
            alert.show();
            return;
        }

        if (model.getProjectRoot() == null || !new File(model.getProjectRoot()).exists()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Please select a Project Root");
            alert.setOnCloseRequest(event1 -> {
                alert.close();
                DirectoryChooser chooser = new DirectoryChooser();
                chooser.setTitle("Choose Project Root");
                File selectedDirectory = chooser.showDialog(Main.getStage());
                if (selectedDirectory != null) {
                    model.setProjectRoot(selectedDirectory.getAbsolutePath());
                    putPref(PROJECT_ROOT, selectedDirectory.getAbsolutePath());
                } else {
                    Alert alert1 = new Alert(Alert.AlertType.ERROR);
                    alert1.setContentText("A Project Root Folder is required");
                }
            });
            alert.show();
        }
        //noinspection unchecked
        for (String string : (List<String>) getPref(ATTRIBUTES))
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
            putPref(SELECTED_SIGN_CONFIG, model.getSignConfig());
            keyTable.getSelectionModel().select(model.getSignConfig());
        } else
            putPref(SELECTED_SIGN_CONFIG, null);

        putPref(FILE_SOURCES, model.getModuleSources());
    }

    public void removeSavedConfig(ActionEvent event) {
        SavedConfigModel model = savedConfigTable.getSelectionModel().getSelectedItem();
        if (model == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Please select a Configuration to delete it");
            alert.show();
            return;
        }
        savedConfigTable.getItems().remove(model);
        SavedConfigModel.removeConfig(model);
    }

    public void resetCurrentPrefs(ActionEvent event) {

    }
}
