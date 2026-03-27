package org.example;


import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("WatchYoJet");

        // Label to display messages
        Label infoLabel = new Label("Welcome to WatchYoJet!");

        // Buttons
        Button btn1 = new Button("Start Radar");
        Button btn2 = new Button("Stop Radar");
        Button btn3 = new Button("Show Flights");

        // Button actions
        btn1.setOnAction(e -> infoLabel.setText("Radar started..."));
        btn2.setOnAction(e -> infoLabel.setText("Radar stopped."));
        btn3.setOnAction(e -> infoLabel.setText("Displaying all flights."));

        // Layout using VBox
        VBox root = new VBox(10); // 10px spacing
        root.setPadding(new Insets(20));
        root.getChildren().addAll(infoLabel, btn1, btn2, btn3);

        // Scene
        Scene scene = new Scene(root, 400, 250);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}