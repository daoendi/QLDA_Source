package org.example.htmlfx.user;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.example.htmlfx.SceneController;
import org.example.htmlfx.dashboard.DashboardControl;
import org.example.htmlfx.toolkits.DatabaseConnection;
import org.example.htmlfx.toolkits.ParentControllerAware;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.example.htmlfx.toolkits.Alert.showAlert;
import static org.example.htmlfx.toolkits.Checked.*;

public class Admin_Edit implements ParentControllerAware {
    private DashboardControl parentController;

    @Override
    public void setParentController(DashboardControl parentController) {
        this.parentController = parentController;
    }

    @FXML
    private TextField info_birthday;

    @FXML
    private TextField info_email;

    @FXML
    private TextField info_firstname;

    @FXML
    private ComboBox<String> info_gender;

    @FXML
    private TextField info_id;

    @FXML
    private TextField info_lastname;

    @FXML
    private TextField info_nickname;

    @FXML
    private TextField info_password;

    @FXML
    private TextField info_phone;

    @FXML
    private Text image;

    private String s;

    private Admin edittingAdmin;

    public void initialize() {
        edittingAdmin = SceneController.getAdmin();

        info_gender.getItems().addAll("Male", "Female", "Other");
        info_birthday.setText(edittingAdmin.getBirthday());
        info_gender.setValue(edittingAdmin.getGender());
        info_phone.setText(edittingAdmin.getPhone());
        info_firstname.setText(edittingAdmin.getFirstname());
        info_lastname.setText(edittingAdmin.getLastname());
        info_id.setText(edittingAdmin.getId());
        info_password.setText(edittingAdmin.getPassword());
        info_email.setText(edittingAdmin.getEmail());
        info_nickname.setText(edittingAdmin.getAdmin_name());

        info_email.setEditable(false);
        info_id.setEditable(false);
        info_nickname.setEditable(false);
    }

    @FXML
    void save_update(ActionEvent event) throws SQLException {
        String email = edittingAdmin.getEmail();

        if (info_birthday.getText().isEmpty()
            || info_gender.getValue().isEmpty()
            || info_phone.getText().isEmpty()
            || info_firstname.getText().isEmpty()
            || info_lastname.getText().isEmpty()
            || info_phone.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "All fields must be filled.");
            return;
        }

        if (!isValidDate(info_birthday.getText())) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Invalid date format. Use yyyy-MM-dd.");
            return;
        }

        if (!isValidPhone(info_phone.getText())) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Phone number must start with '0' and have 10 digits.");
            return;
        }

        String updateAdminSQL = "UPDATE Admins " +
                "SET firstname = ?, lastname = ?, gender = ?, birth = ?, phone = ? , password = ?, image = ?" +
                "WHERE email = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement updateAdminStatement = connection.prepareStatement(updateAdminSQL)) {

            // Cập nhật thông tin mới từ các trường trong form
            String newFirstname = info_firstname.getText();
            String newLastname = info_lastname.getText();
            String newGender = info_gender.getValue();
            String newBirthday = info_birthday.getText();
            String newPhone = info_phone.getText();
            String newPassword = info_password.getText();
            String newImage = s;

            updateAdminStatement.setString(1, newFirstname);
            updateAdminStatement.setString(2, newLastname);
            updateAdminStatement.setString(3, newGender);
            updateAdminStatement.setString(4, newBirthday);
            updateAdminStatement.setString(5, newPhone);
            updateAdminStatement.setString(6, newPassword);
            updateAdminStatement.setString(7, newImage);
            updateAdminStatement.setString(8, email);

            int rowsAffected = updateAdminStatement.executeUpdate();
            if (rowsAffected > 0) {
                edittingAdmin.setFirstname(newFirstname);
                edittingAdmin.setLastname(newLastname);
                edittingAdmin.setGender(newGender);
                edittingAdmin.setBirthday(newBirthday);
                edittingAdmin.setPhone(newPhone);
                edittingAdmin.setPassword(newPassword);
                edittingAdmin.setEmail(email);
                edittingAdmin.setImage(newImage);

                parentController.update(edittingAdmin);


                showAlert(Alert.AlertType.INFORMATION, "Success", "User updated successfully.");

                // Cập nhật pane2 với thông tin mới nếu parentController không null
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update user.");
            }

            // Đóng cửa sổ hiện tại
            Stage stage = (Stage) info_email.getScene().getWindow();
            stage.close();

            // Cập nhật TableView trên giao diện cha nếu có
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
        }
    }


    @FXML
    void back(ActionEvent event) {
        Stage stage = (Stage) info_email.getScene().getWindow();
        stage.close();
    }

    @Override
    public void setParentController(Member_controller parentController) {

    }

    @FXML
    public void pick1() {
        image.setText("Change avt No.1");
        s="/img/avt/admin/image1.jpg";
    }

    @FXML
    public void pick2() {
        image.setText("Change avt No.2");
        s="/img/avt/admin/image2.jpg";
    }

    @FXML
    public void pick3() {
        image.setText("Change avt No.3");
        s="/img/avt/admin/image3.jpg";
    }

    @FXML
    public void pick4() {
        image.setText("Change avt No.4");
        s="/img/avt/admin/image4.jpg";
    }

}
