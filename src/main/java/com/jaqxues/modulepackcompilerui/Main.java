package com.jaqxues.modulepackcompilerui;

import com.jaqxues.modulepackcompilerui.preferences.PreferenceManager;

import java.lang.ref.WeakReference;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * This file was created by Jacques (jaqxues) in the Project ModulePackCompilerUI.<br>
 * Date: 2.10.2018 - Time 18:31.
 */

public class Main extends Application {
    @Override
    public void stop() throws Exception {
        super.stop();
        Platform.exit();
        System.exit(0);
    }

    private static WeakReference<Stage> stage;

    public static void main(String[] args) {
        launch(args);
    }

    public static Stage getStage() {
        return stage.get();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        PreferenceManager.init();

        Parent root = FXMLLoader.load(getClass().getResource("/layout.fxml"));
        primaryStage.setTitle("ModulePack Compiler UI");
        primaryStage.getIcons().addAll(
                new Image("images/module_yellow_50.png"),
                new Image("images/module_yellow_100.png"),
                new Image("images/module_yellow_500.png")
        );
        primaryStage.setMinWidth(1200d);
        primaryStage.setMinHeight(900d);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        stage = new WeakReference<>(primaryStage);
    }
}
