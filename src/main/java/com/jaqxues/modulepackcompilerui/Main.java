package com.jaqxues.modulepackcompilerui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private static Stage stage;

    public static void main(String[] args) {
        launch(args);
    }

    public static Stage getStage() {
        return stage;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        PreferenceManager.init();

        Parent root2 = FXMLLoader.load(getClass().getResource("/layout.fxml"));
        primaryStage.setTitle("ModulePack Compiler UI");
        primaryStage.setScene(new Scene(root2));
        primaryStage.show();

        stage = primaryStage;
    }
}
