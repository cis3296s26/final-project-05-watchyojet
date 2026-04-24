package com.watchyojet;

import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import javafx.scene.control.ListView;
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

        WYJAppController controller = fxmlLoader.getController();
        try { controller.handleThemeChange(); } catch (Exception ignored) {}
        controller.webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                new Thread(controller.spawnMainThread).start();
            }
        });

    }
    @Override
    public void stop() {
        System.out.println("Killing Process...");

        System.exit(0);
    }
    public static void main(String[] args) {
        launch();
    }
}
