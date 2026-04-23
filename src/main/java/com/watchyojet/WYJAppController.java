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
import java.util.List;
import java.util.Locale;
import com.watchyojet.model.Aircraft;

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
            logList.scrollTo(logList.getItems().size() - 1);
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
    public void handleClose() {
        Platform.exit();
    }

    @FXML
    public void handleThemeChange(){
        Scene scene = topContainer.getScene();
        scene.getStylesheets().clear();
        if(!light.isSelected()){
            String dark = getClass().getResource("/dark.css").toExternalForm();
            scene.getStylesheets().add(dark);
        }else{
            String lightCss = getClass().getResource("/light.css").toExternalForm();
            scene.getStylesheets().add(lightCss);
        }

    }

    public void updateAllAircraft(List<Aircraft> aircrafts) {
        if (aircrafts.isEmpty()) return;
        Platform.runLater(() -> {
            Object check = webEngine.executeScript("typeof batchUpdateAircraft !== 'undefined'");
            if (!check.equals(true)) return;
            StringBuilder sb = new StringBuilder("batchUpdateAircraft([");
            boolean first = true;
            for (Aircraft a : aircrafts) {
                if (!first) sb.append(',');
                first = false;
                String cs = a.getCallsign()
                        .replace("\\", "\\\\")
                        .replace("'", "\\'");
                sb.append(String.format(Locale.US, "{cs:'%s',lat:%f,lon:%f,alt:%f,spd:%f,hdg:%f}",
                        cs, a.getLat(), a.getLon(), a.getAltitude(), a.getSpeed(), a.getHeading()));
            }
            sb.append("])");
            webEngine.executeScript(sb.toString());
        });
    }

    public void markConflict(String cs1, String cs2) {
        Platform.runLater(() -> {
            Object check = webEngine.executeScript("typeof markConflict !== 'undefined'");
            if (check.equals(true)) {
                String s1 = cs1.replace("'", "\\'"), s2 = cs2.replace("'", "\\'");
                webEngine.executeScript(String.format("markConflict('%s','%s')", s1, s2));
            }
        });
    }

    public void logToMap(String message) {
        Platform.runLater(() -> {
            Object check = webEngine.executeScript("typeof logATCEvent !== 'undefined'");
            if (check.equals(true)) {
                String safe = message.replace("'", "\\'").replace("\n", " ");
                webEngine.executeScript(String.format("logATCEvent('%s')", safe));
            }
        });
    }

}