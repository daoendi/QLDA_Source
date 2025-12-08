package org.example.htmlfx.toolkits;

import javafx.event.ActionEvent;
import org.example.htmlfx.SwitchScene;

import javafx.scene.input.MouseEvent;

public class MenuEvent {
    public static void gotoHome(MouseEvent event) {
        SwitchScene sw = new SwitchScene();
        sw.switchScene(event, "/org/example/htmlfx/dashboard/Dashboard.fxml");
    }

    public static void gotoBook(MouseEvent event) {
        SwitchScene sw = new SwitchScene();
        sw.switchScene(event, "/org/example/htmlfx/book/Book.fxml");
    }

    public static void gotoMember(MouseEvent event) {
        SwitchScene sw = new SwitchScene();
        sw.switchScene(event, "/org/example/htmlfx/user/Members.fxml");
    }

    public static void gotoBorrow(MouseEvent event) {
        SwitchScene sw = new SwitchScene();
        sw.switchScene(event, "/org/example/htmlfx/borrow/Borrow.fxml");
    }

    public static void gotoIncome(MouseEvent event) {
        SwitchScene sw = new SwitchScene();
        sw.switchScene(event, "/org/example/htmlfx/income/Income.fxml");
    }

    public static void gotoLogin(MouseEvent event) {
        SwitchScene sw = new SwitchScene();
        sw.switchScene(event, "/org/example/htmlfx/Start.fxml");
    }

}
