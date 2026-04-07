package com.watchyojet;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import javafx.stage.Stage;

import java.io.IOException;

public class WYJApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(WYJApp.class.getResource("/test-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("WatchYoJet");
        stage.setScene(scene);
        stage.show();
        System.out.println("Creating Window");
    }

    public static void main(String[] args) {
        launch();
    }
}
