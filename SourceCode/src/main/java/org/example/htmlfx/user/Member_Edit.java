package org.example.htmlfx.user;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.htmlfx.dashboard.DashboardControl;
import org.example.htmlfx.toolkits.DatabaseConnection;
import org.example.htmlfx.toolkits.ParentControllerAware;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.example.htmlfx.toolkits.Alert.showAlert;
import static org.example.htmlfx.toolkits.Checked.*;

public class Member_Edit implements ParentControllerAware {
    private Member_controller parentController;

    @Override
    public void setParentController(DashboardControl parentController) {

    }

    @Override
    public void setParentController(Member_controller parentController) {
        this.parentController = parentController;
    }

    @FXML
    private TextField info_birthday;

    @FXML
    private TextField info_email;

    @FXML
    private ComboBox<String> info_gender;

    @FXML
    private TextField info_lastname;

    @FXML
    private TextField info_phone;

    @FXML
    private TextField info_firstname;

    private Member edittingMember;

    public void initialize() {
        edittingMember = Member_controller.getCurrentMember();

        info_gender.getItems().addAll("Male", "Female", "Other");

        info_birthday.setText(edittingMember.getBirthday());
        info_email.setText(edittingMember.getEmail());
        info_gender.setValue(edittingMember.getGender());
        info_phone.setText(edittingMember.getPhone());
        info_firstname.setText(edittingMember.getFirstname());
        info_lastname.setText(edittingMember.getLastname());
    }

    @FXML
    void save_update(ActionEvent event) throws SQLException {
        String id = edittingMember.getId();

        if (info_birthday.getText().isEmpty()
                || info_email.getText().isEmpty()
                || info_gender.getValue().isEmpty()
                || info_phone.getText().isEmpty()
                || info_firstname.getText().isEmpty()
                || info_lastname.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "All fields must be filled.");
            return;
        }

        if (!isValidDate(info_birthday.getText())) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Invalid date format. Use yyyy-MM-dd.");
            return;
        }

        if (!isValidEmail(info_email.getText())) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Invalid email format.");
            return;
        }

        if (!isValidPhone(info_phone.getText())) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Phone number must start with '0' and have 10 digits.");
            return;
        }

        String updateMemberSQL = "UPDATE members " +
                "SET firstname = ?, lastname = ?, gender = ?, birth = ?, email = ?, phone = ? " +
                "WHERE member_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement updateMemberStatement = connection.prepareStatement(updateMemberSQL)) {

            // Cập nhật thông tin mới từ các trường trong form
            String newFirstname = info_firstname.getText();
            String newLastname = info_lastname.getText();
            String newGender = info_gender.getValue();
            String newBirthday = info_birthday.getText();
            String newEmail = info_email.getText();
            String newPhone = info_phone.getText();
            String image = edittingMember.getImage();

            updateMemberStatement.setString(1, newFirstname);
            updateMemberStatement.setString(2, newLastname);
            updateMemberStatement.setString(3, newGender);
            updateMemberStatement.setString(4, newBirthday);
            updateMemberStatement.setString(5, newEmail);
            updateMemberStatement.setString(6, newPhone);
            updateMemberStatement.setString(7, id);

            int rowsAffected = updateMemberStatement.executeUpdate();
            if (rowsAffected > 0) {
                edittingMember.setFirstname(info_firstname.getText());
                edittingMember.setLastname(info_lastname.getText());
                edittingMember.setGender(info_gender.getValue());
                edittingMember.setBirthday(info_birthday.getText());
                edittingMember.setEmail(info_email.getText());
                edittingMember.setPhone(info_phone.getText());

                showAlert(Alert.AlertType.INFORMATION, "Success", "User updated successfully.");

                // Cập nhật pane2 với thông tin mới nếu parentController không null
                if (parentController != null) {
                    Member updatedMember = new Member(id, newFirstname, newLastname, newGender, newBirthday, newEmail, newPhone, image);
                    parentController.updatePane2(updatedMember);
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update user.");
            }

            // Đóng cửa sổ hiện tại
            Stage stage = (Stage) info_gender.getScene().getWindow();
            stage.close();

            // Cập nhật TableView trên giao diện cha nếu có
            if (parentController != null) {
                parentController.updateTableView();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
        }
    }


    @FXML
    void back(ActionEvent event) {
        Stage stage = (Stage) info_birthday.getScene().getWindow();
        stage.close();
    }
}
