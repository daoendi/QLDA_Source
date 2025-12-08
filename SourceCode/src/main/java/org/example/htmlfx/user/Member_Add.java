package org.example.htmlfx.user;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import org.example.htmlfx.dashboard.DashboardControl;
import org.example.htmlfx.toolkits.DatabaseConnection;
import org.example.htmlfx.toolkits.ParentControllerAware;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.example.htmlfx.toolkits.Alert.showAlert;
import static org.example.htmlfx.toolkits.Checked.*;

public class Member_Add implements ParentControllerAware {
    private Member_controller parentController;

    @Override
    public void setParentController(DashboardControl parentController) {

    }

    @Override
    public void setParentController(Member_controller parentController) {
        this.parentController = parentController;
    }

    @FXML
    private TextField birthday;

    @FXML
    private TextField email;

    @FXML
    private TextField firstname;

    @FXML
    private ComboBox<String> gender;

    @FXML
    private TextField lastname;

    @FXML
    private TextField phone;

    @FXML
    public void initialize() {
        gender.getItems().addAll("Male", "Female", "Other");
    }

    @FXML
    void addNewUser(ActionEvent event) {
        if (birthday.getText().isEmpty()
                || firstname.getText().isEmpty()
                || lastname.getText().isEmpty()
                || phone.getText().isEmpty()
                || email.getText().isEmpty()
                || gender.getValue() == null) {
            showAlert(AlertType.WARNING, "Warning", "All fields must be filled.");
            return;
        }

        if (!isValidDate(birthday.getText())) {
            showAlert(AlertType.WARNING, "Warning", "Invalid date format. Use yyyy-MM-dd.");
            return;
        }

        if (!isValidEmail(email.getText())) {
            showAlert(AlertType.WARNING, "Warning", "Invalid email format.");
            return;
        }

        if (!isValidPhone(phone.getText())) {
            showAlert(AlertType.WARNING, "Warning", "Phone number must start with '0' and have 10 digits.");
            return;
        }

        String insertUserSQL = "INSERT INTO members (firstname, lastname, gender, birth, phone, email) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement insertUserStatement = connection.prepareStatement(insertUserSQL)) {

            insertUserStatement.setString(1, firstname.getText());
            insertUserStatement.setString(2, lastname.getText());
            insertUserStatement.setString(3, gender.getValue());
            insertUserStatement.setString(4, birthday.getText());
            insertUserStatement.setString(5, phone.getText());
            insertUserStatement.setString(6, email.getText());

            int rowsAffected = insertUserStatement.executeUpdate();
            if (rowsAffected > 0) {
                showAlert(AlertType.INFORMATION, "Success", "User added successfully.");

                Stage stage = (Stage) firstname.getScene().getWindow();
                stage.close();

                if (parentController != null) {
                    parentController.updateTableView();
                }
            } else {
                showAlert(AlertType.ERROR, "Error", "Failed to add user.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
        }
    }


    @FXML
    void backToUsers(ActionEvent event) {
        Stage stage = (Stage) firstname.getScene().getWindow();
        stage.close();
    }
}
