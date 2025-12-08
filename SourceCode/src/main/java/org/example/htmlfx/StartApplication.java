package org.example.htmlfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import javafx.stage.Stage;
import org.example.htmlfx.dashboard.Notification_Service;

public class StartApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/org/example/htmlfx/Start.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setX(200);
        stage.setY(120);

        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {

        Notification_Service notificationService = new Notification_Service();
        notificationService.start();

        launch(args);
    }
}
