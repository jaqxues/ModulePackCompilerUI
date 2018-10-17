package com.jaqxues.modulepackcompilerui;

import com.jaqxues.modulepackcompilerui.preferences.PreferenceManager;

import java.lang.ref.WeakReference;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {
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
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        stage = new WeakReference<>(primaryStage);
    }
}
