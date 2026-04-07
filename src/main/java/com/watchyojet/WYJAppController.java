package com.watchyojet;


import javafx.fxml.FXML;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URL;

public class WYJAppController {

    @FXML
    private WebView webView;

    private WebEngine webEngine;

    @FXML
    public void initialize() {
        // Initialize WebEngine here
        webEngine = webView.getEngine();

        // Load a webpage (can be local or remote)
        URL url = getClass().getResource("/map.html"); // local HTML
        if (url != null) {
            webEngine.load(url.toExternalForm());
        } else {
            webEngine.load("https://chivalry2.com"); // fallback URL
        }
    }
}