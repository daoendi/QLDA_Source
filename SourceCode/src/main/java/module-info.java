module org.example.htmlfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires com.almasb.fxgl.all;
    requires java.desktop;
    requires java.sql;
    requires mysql.connector.j;
    requires okhttp3;
    requires google.api.client;
    requires com.google.api.client;
    requires com.google.api.client.json.gson;
    requires com.google.gson;
    requires org.json;
    requires java.net.http;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires javafx.media;

    // Xuất các gói cần thiết cho module khác
    exports org.example.htmlfx;
    exports org.example.htmlfx.book;
    exports org.example.htmlfx.user;  // Đổi User -> user
    exports org.example.htmlfx.dashboard; // Đổi Dashboard -> dashboard
    exports org.example.htmlfx.borrow;
    // Mở các gói cần thiết cho JavaFX FXML

    opens org.example.htmlfx to javafx.fxml;
    opens org.example.htmlfx.book to javafx.fxml;
    opens org.example.htmlfx.user to javafx.fxml;  // Đổi User -> user
    opens org.example.htmlfx.dashboard to javafx.fxml;
    exports org.example.htmlfx.income;

    opens org.example.htmlfx.income to javafx.fxml;  // Đổi Dashboard -> dashboard
    opens org.example.htmlfx.borrow to javafx.fxml;
    exports org.example.htmlfx.toolkits;
    opens org.example.htmlfx.toolkits to javafx.fxml;
}
