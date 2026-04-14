module com.watchyojet {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires jdk.crypto.cryptoki;
    requires jdk.crypto.ec;
    requires com.fasterxml.jackson.databind;
    requires java.net.http;

    opens com.watchyojet to javafx.fxml;
    exports com.watchyojet;

}