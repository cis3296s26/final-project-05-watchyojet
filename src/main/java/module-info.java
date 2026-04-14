module com.watchyojet {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires java.net.http;                 
    requires com.fasterxml.jackson.databind; 

    requires jdk.crypto.cryptoki;
    requires jdk.crypto.ec;

    opens com.watchyojet to javafx.fxml;

    
    opens com.watchyojet.model to com.fasterxml.jackson.databind;

    exports com.watchyojet;
}