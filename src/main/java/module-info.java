module com.watchyojet {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires jdk.crypto.cryptoki;
    requires jdk.crypto.ec;

    opens com.watchyojet to javafx.fxml;
    exports com.watchyojet;

}