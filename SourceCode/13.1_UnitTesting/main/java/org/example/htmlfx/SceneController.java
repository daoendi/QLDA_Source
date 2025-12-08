package org.example.htmlfx;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.animation.TranslateTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;
import org.example.htmlfx.toolkits.DatabaseConnection;
import org.example.htmlfx.toolkits.Music;
import org.example.htmlfx.user.Admin;

import java.io.IOException;
import java.sql.*;

import static org.example.htmlfx.toolkits.Alert.*;
import static org.example.htmlfx.toolkits.Checked.*;

public class SceneController {
    private boolean onSignin;

    @FXML
    private VBox BackVbox;

    @FXML
    private VBox HelloVbox;

    @FXML
    private VBox createVbox;

    @FXML
    private TextField email;

    @FXML
    private TextField email_signin;

    @FXML
    private TextField name;

    @FXML
    private PasswordField password;

    @FXML
    private PasswordField password_signin;

    @FXML
    private VBox signInVbox;

    @FXML
    private StackPane stackPaneLeft;

    @FXML
    private StackPane stackPaneRight;

    private static Admin admin;
    private static Music music;

    @FXML
    public void initialize() {
        createVbox.setVisible(false);
        BackVbox.setVisible(false);
        signInVbox.setVisible(true);
        HelloVbox.setVisible(true);
        onSignin = true;
        admin = new Admin();
    }

    public static Admin getAdmin() {
        return admin;
    }

    public static Music getMusic() {
        return music;
    }

    public static void setMusic(Music m) {
        music = m;
    }

    public static void setAdmin(Admin admin) {
        SceneController.admin = admin;
    }

    public void handleSwitchScene(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/htmlfx/dashboard/Dashboard.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void togglePosition() {
        TranslateTransition transitionRight = new TranslateTransition(Duration.seconds(0.5), stackPaneRight);
        TranslateTransition transitionLeft = new TranslateTransition(Duration.seconds(0.5), stackPaneLeft);

        if (onSignin) {
            transitionRight.setByX(-500);
            transitionLeft.setByX(400);

            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.seconds(0.25), event -> {
                        signInVbox.setVisible(false);
                        createVbox.setVisible(true);
                        HelloVbox.setVisible(false);
                        BackVbox.setVisible(true);
                    })
            );

            transitionRight.play();
            transitionLeft.play();
            timeline.play();

        } else {
            transitionRight.setByX(500);
            transitionLeft.setByX(-400);

            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.seconds(0.25), event -> {
                        signInVbox.setVisible(true);
                        createVbox.setVisible(false);
                        HelloVbox.setVisible(true);
                        BackVbox.setVisible(false);
                    })
            );

            transitionRight.play();
            transitionLeft.play();
            timeline.play();
        }

        onSignin = !onSignin;
    }

    @FXML
    private void handleSignIn(ActionEvent event) {
        if (!onSignin) {
            togglePosition();
            clearData();
        } else {
            if (email_signin.getText().isEmpty() || password_signin.getText().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Warning", "All fields must be filled.");
                return;
            }

            String credential = email_signin.getText();
            String passwordQuery = "SELECT * FROM admins WHERE (email = ? OR admin_name = ?) AND password = ?";

            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement passwordStatement = connection.prepareStatement(passwordQuery)) {

                // Kiểm tra mật khẩu
                passwordStatement.setString(1, credential);
                passwordStatement.setString(2, credential);
                passwordStatement.setString(3, password_signin.getText());
                ResultSet passwordResultSet = passwordStatement.executeQuery();

                if (passwordResultSet.next()) {
                    // Mật khẩu chính xác, đăng nhập thành công
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Login success");

                    String id = passwordResultSet.getString("admin_id");
                    String firstName = passwordResultSet.getString("firstName");
                    String lastName = passwordResultSet.getString("lastName");
                    String email = passwordResultSet.getString("email");
                    String password = passwordResultSet.getString("password");
                    String name = passwordResultSet.getString("admin_name");
                    String gender = passwordResultSet.getString("gender");
                    String phone = passwordResultSet.getString("phone");
                    String image = passwordResultSet.getString("image");
                    String birth = passwordResultSet.getString("birth");

                    admin = new Admin(id, firstName, lastName, gender, birth, email, phone, password, name, image);

                    if (music == null) {
                        music = new Music();
                        music.play();
                    }

                    handleSwitchScene(event);
                } else {
                    // Mật khẩu không chính xác hoặc tài khoản không tồn tại
                    showAlert(Alert.AlertType.ERROR, "Error", "Invalid credentials or account does not exist.");
                    clearData();
                }

            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSignUp(ActionEvent event) {
        if (!onSignin) {
            if (name.getText().isEmpty() || email.getText().isEmpty() || password.getText().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Warning", "All fields must be filled.");
                return;
            }

            if (!isValidEmail(email.getText())) {
                showAlert(Alert.AlertType.INFORMATION, "Info", "Invalid email address. \n.............@gmail.com");
                return;
            }

            String emailQuery = "SELECT * FROM admins WHERE email = ?";
            String nameQuery = "SELECT * FROM admins WHERE admin_name = ?";
            String sql = "INSERT INTO admins (admin_name, email, password) VALUES (?, ?, ?)";

            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement emailStatement = connection.prepareStatement(emailQuery);
                 PreparedStatement nameStatement = connection.prepareStatement(nameQuery);
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                emailStatement.setString(1, email.getText());
                ResultSet emailResultSet = emailStatement.executeQuery();

                if (emailResultSet.next()) {
                    showAlert(Alert.AlertType.INFORMATION, "Warning", "Email already exists.\nPlease enter another.");
                    email.setText("");
                    return;
                }

                nameStatement.setString(1, name.getText());
                ResultSet nameResultSet = nameStatement.executeQuery();

                if (nameResultSet.next()) {
                    showAlert(Alert.AlertType.INFORMATION, "Warning", "Name already exists.\nPlease enter another.");
                    name.setText("");
                    return;
                }

                statement.setString(1, name.getText());
                statement.setString(2, email.getText());
                statement.setString(3, password.getText());
                int rowsInserted = statement.executeUpdate();

                if (rowsInserted > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Sign up success.");
                    clearData();
                    togglePosition(); // Chuyển vị trí sau khi đăng ký thành công
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Sign up failed.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
            }
        } else {
            togglePosition();
        }
    }

    private void clearData() {
        email.setText("");
        password.setText("");
        name.setText("");
        email_signin.setText("");
        password_signin.setText("");
    }
}
