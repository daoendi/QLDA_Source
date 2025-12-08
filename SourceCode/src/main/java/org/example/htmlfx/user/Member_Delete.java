package org.example.htmlfx.user;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.htmlfx.dashboard.DashboardControl;
import org.example.htmlfx.toolkits.DatabaseConnection;
import org.example.htmlfx.toolkits.ParentControllerAware;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Member_Delete implements ParentControllerAware {
    private Member_controller parentController;

    @Override
    public void setParentController(DashboardControl parentController) {

    }

    @Override
    public void setParentController(Member_controller parentController) {
        this.parentController = parentController;
    }

    @FXML
    private TextField member_ID;

    @FXML
    void backToMember(ActionEvent event) {
        Stage stage = (Stage) member_ID.getScene().getWindow();
        stage.close();
    }

    @FXML
    void deleteMember(ActionEvent event) {
        if (member_ID.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING,"Warning", "Member ID cannot be empty.");
            return;
        }

        String memberId = member_ID.getText();

        String checkMemberSQL = "SELECT COUNT(*) FROM members WHERE member_id = ?";
        String deleteMemberSQL = "DELETE FROM members WHERE member_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement checkMemberStatement = connection.prepareStatement(checkMemberSQL);
             PreparedStatement deleteMemberStatement = connection.prepareStatement(deleteMemberSQL)) {

            // Kiểm tra xem ID có tồn tại hay không
            checkMemberStatement.setString(1, memberId);
            ResultSet resultSet = checkMemberStatement.executeQuery();
            resultSet.next();

            if (resultSet.getInt(1) == 0) {
                showAlert(Alert.AlertType.WARNING, "Warning", "Member ID does not exist.");
                return;
            }

            // Xóa thành viên
            deleteMemberStatement.setInt(1, Integer.parseInt(memberId));
            int rowsAffected = deleteMemberStatement.executeUpdate();

            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Member deleted successfully.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete the member.");
            }

            // Đóng cửa sổ sau khi thực hiện xóa
            Stage stage = (Stage) member_ID.getScene().getWindow();
            stage.close();

            if (parentController != null) {
                parentController.updateTableView();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
