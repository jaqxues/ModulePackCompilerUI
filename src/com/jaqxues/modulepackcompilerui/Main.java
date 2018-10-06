package com.jaqxues.modulepackcompilerui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private static String currentPath;

    @Override
    public void start(Stage primaryStage) throws Exception{
        currentPath = getParameters().getNamed().get("CURRENT_PATH");
        Parent root = FXMLLoader.load(getClass().getResource("layout.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 900, 600));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

    public static String getCurrentPath() {
        return currentPath;
    }
}
