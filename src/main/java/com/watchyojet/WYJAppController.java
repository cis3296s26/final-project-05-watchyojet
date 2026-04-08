package com.watchyojet;


import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.application.Platform;

import java.net.URL;

public class WYJAppController {
    private static WYJAppController instance;
    public static WYJAppController getInstance() {
        return instance;
    }

    @FXML
    private VBox topContainer;

    @FXML
    private WebView webView;

    @FXML
    private ListView<String> logList;

    @FXML
    private CheckMenuItem showLogs;

    @FXML
    private RadioMenuItem light;

    public WebEngine webEngine;

    @FXML
    public void initialize() {
        instance = this;
        // Initialize WebEngine here
        webEngine = webView.getEngine();

        // Load a webpage (can be local or remote)
        URL url = getClass().getResource("/map.html"); // local HTML
        if (url != null) {
            webEngine.load(url.toExternalForm());
        } else {
            webEngine.load("https://chivalry2.com"); // fallback URL
        }

        logList.getItems().add("Log 1");
        logList.getItems().add("Log 2");
        logList.getItems().add("Log 3");
        logList.getItems().add("Log 4");
        logList.getItems().add("Log 5");
        logList.getItems().add("Log 6");
        logList.scrollTo(logList.getItems().size());

        //handleThemeChange();
    }

    @FXML
    public void toggleLogs(){
            logList.setVisible(showLogs.isSelected());
            logList.setManaged(showLogs.isSelected());
    }

    public void log(String message) {
        Platform.runLater(() -> {//keeps the UI update from running on the LOGIC thread
            logList.getItems().add(message);
            logList.scrollTo(logList.getItems().size());
        });
    }

    Task<Void> spawnMainThread = new Task<>(){
        @Override
        protected Void call() throws Exception{
            Main.main(null);
            return null;
        }
    };

    @FXML
    public void handleThemeChange(){
        Scene scene = topContainer.getScene();
        scene.getStylesheets().clear();
        if(!light.isSelected()){
            String dark = getClass().getResource("/dark.css").toExternalForm();
            scene.getStylesheets().add(dark);
        }else{
            String light = getClass().getResource("/light.css").toExternalForm();
            scene.getStylesheets().add(light);
        }

    }

    public void updateMapPoint(String id, double lat, double lng) {
        Platform.runLater(() -> {
            // Check if the script exists before calling it
            Object check = webEngine.executeScript("typeof updateAircraft !== 'undefined'");
            if (check.equals(true)) {
                String script = String.format("updateAircraft('%s', %f, %f)", id, lat, lng);
                webEngine.executeScript(script);
            }
        });
    }

}