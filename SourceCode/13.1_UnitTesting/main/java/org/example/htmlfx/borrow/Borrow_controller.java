package org.example.htmlfx.borrow;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import org.example.htmlfx.toolkits.DatabaseConnection;
import org.example.htmlfx.toolkits.MenuEvent;

import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static org.example.htmlfx.toolkits.Alert.showAlert;

public class Borrow_controller implements Initializable {

    @FXML
    private TableColumn<Borrow, String> C_ID;

    @FXML
    private TableColumn<Borrow, String> C_bookid;

    @FXML
    private TableColumn<Borrow, String> C_bookname;

    @FXML
    private TableColumn<Borrow, String> C_borrow;

    @FXML
    private TableColumn<Borrow, String> C_memid;

    @FXML
    private TableColumn<Borrow, String> C_return;

    @FXML
    private TableColumn<Borrow, String> C_status;

    @FXML
    private Pane pane1;

    @FXML
    private Pane pane2;

    @FXML
    private TableView<Borrow> table_of_borrow;

    @FXML
    private TextField day;

    @FXML
    private TextField month;

    @FXML
    private TextField year;

    @FXML
    private Text info_birthday;

    @FXML
    private Text info_email;

    @FXML
    private Text info_gender;

    @FXML
    private Text info_id;

    @FXML
    private Text info_name;

    @FXML
    private Text info_phone;

    @FXML
    private Text bookname;

    @FXML
    private TextField member_field;

    @FXML
    private TextField book_field;

    public static boolean check = true;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        C_ID.setCellValueFactory(new PropertyValueFactory<Borrow, String>("id"));
        C_bookid.setCellValueFactory(new PropertyValueFactory<Borrow, String>("book_id"));
        C_bookname.setCellValueFactory(new PropertyValueFactory<Borrow, String>("name"));
        C_borrow.setCellValueFactory(new PropertyValueFactory<Borrow, String>("borrowDate"));
        C_memid.setCellValueFactory(new PropertyValueFactory<Borrow, String>("member_id"));
        C_return.setCellValueFactory(new PropertyValueFactory<Borrow, String>("returnDate"));
        C_status.setCellValueFactory(new PropertyValueFactory<Borrow, String>("status"));

        if (check) {
            ObservableList<Borrow> borrows = FXCollections.observableArrayList(Borrow_controller.getBorrow());
            table_of_borrow.setItems(borrows);
        } else {
            ObservableList<Borrow> borrows = FXCollections.observableArrayList(filter_temp());
            table_of_borrow.setItems(borrows);
            check = true;
        }

        ContextMenu contextMenu = new ContextMenu();
        MenuItem updateItem = new MenuItem("Returned");
        updateItem.setOnAction(event -> {
            Borrow selectedBorrow = table_of_borrow.getSelectionModel().getSelectedItem();
            if (selectedBorrow != null) {
                update(selectedBorrow);
                updateTableOfBorrow();
            }
        });
        contextMenu.getItems().addAll(updateItem);

        // Thêm sự kiện nhấn chuột phải vào các hàng của TableView
        table_of_borrow.setRowFactory(tv -> {
            TableRow<Borrow> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.SECONDARY && !row.isEmpty()) {
                    contextMenu.show(row, event.getScreenX(), event.getScreenY());
                }
            });
            return row;
        });

        member_field.setOnAction(event -> getInfoMember());

        book_field.setOnAction(event -> getInfoBook());

        pane1.setVisible(true);
        pane2.setVisible(false);
    }

    private void getInfoMember() {
        String checkSql = "SELECT COUNT(*) FROM members WHERE member_id = ?";
        String fetchSql = "SELECT * FROM members WHERE member_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement checkStatement = connection.prepareStatement(checkSql);
             PreparedStatement fetchStatement = connection.prepareStatement(fetchSql)) {

            // Kiểm tra xem member_id có tồn tại không
            checkStatement.setString(1, member_field.getText());
            ResultSet checkResult = checkStatement.executeQuery();
            if (checkResult.next() && checkResult.getInt(1) > 0) {
                // Nếu tồn tại, lấy thông tin chi tiết
                fetchStatement.setString(1, member_field.getText());
                ResultSet resultSet = fetchStatement.executeQuery();
                if (resultSet.next()) {
                    setupMember(resultSet);
                }
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Information", "No member found with this ID.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void getInfoBook() {
        String checkSql = "SELECT COUNT(*) FROM books WHERE book_id = ?";
        String fetchSql = "SELECT * FROM books WHERE book_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement checkStatement = connection.prepareStatement(checkSql);
             PreparedStatement fetchStatement = connection.prepareStatement(fetchSql)) {

            // Kiểm tra xem book_id có tồn tại không
            checkStatement.setString(1, book_field.getText());
            ResultSet checkResult = checkStatement.executeQuery();
            if (checkResult.next() && checkResult.getInt(1) > 0) {
                // Nếu tồn tại, lấy thông tin chi tiết
                fetchStatement.setString(1, book_field.getText());
                ResultSet resultSet = fetchStatement.executeQuery();
                if (resultSet.next()) {
                    setupBook(resultSet);
                }
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Information", "No book found with this ID.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupMember(ResultSet rs) throws SQLException {
        info_id.setText(rs.getString("member_id") != null ? rs.getString("member_id") : "N/A");
        info_name.setText(rs.getString("firstname") + " " + rs.getString("lastname"));
        info_phone.setText(rs.getString("phone") != null ? rs.getString("phone") : "N/A");
        info_email.setText(rs.getString("email") != null ? rs.getString("email") : "N/A");
        info_gender.setText(rs.getString("gender") != null ? rs.getString("gender") : "N/A");
        info_birthday.setText(rs.getString("birth") != null ? rs.getString("birth") : "N/A");
    }

    private void setupBook(ResultSet rs) throws SQLException {
        String name = rs.getString("book_name");
        String remain = rs.getString("remaining_quantity");
        String res = "Name: " + name + "\n" + "Remaining: " + remain;

        bookname.setText(res);
    }

    private void updateTableOfBorrow() {
        ObservableList<Borrow> borrows = FXCollections.observableArrayList(Borrow_controller.getBorrow());
        table_of_borrow.setItems(borrows);
    }

    @FXML
    private void back() {
        pane1.setVisible(true);
        pane2.setVisible(false);

        updateTableOfBorrow();

        resetData();
    }

    @FXML
    private void save() {
        // Kiểm tra nếu các trường member_field hoặc book_field chưa được điền
        if (member_field.getText().isEmpty() || book_field.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please fill in both Member ID and Book ID fields.");
            return; // Dừng lại nếu thiếu thông tin
        }

        String sql = "INSERT INTO borrow (member_id, book_id, borrow_date) VALUES (?, ?, CURRENT_DATE)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, member_field.getText());
            statement.setString(2, book_field.getText());
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Record saved successfully.");
            } else {
                showAlert(Alert.AlertType.WARNING, "Warning", "No record was saved.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
        }

        updateTableOfBorrow();

        pane1.setVisible(true);
        pane2.setVisible(false);

        resetData();
    }

    public void update(Borrow borrow) {
        String sql = "UPDATE borrow SET returned_date = CURRENT_DATE WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement updateStatement = connection.prepareStatement(sql)) {

            updateStatement.setString(1, borrow.getId());
            int rowsAffected = updateStatement.executeUpdate();

            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Borrow record updated successfully.");
            } else {
                showAlert(Alert.AlertType.WARNING, "Warning", "No matching borrow record found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error during update: " + e.getMessage());
        }
    }

    public void update_filter() {
        ObservableList<Borrow> borrows = FXCollections.observableArrayList(filter());
        table_of_borrow.setItems(borrows);
    }

    public static List<Borrow> filter_temp() {
        List<Borrow> borrows = new ArrayList<>();
        String sql = "SELECT * FROM borrow WHERE borrow_date = CURRENT_DATE";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String id = resultSet.getString("id");
                String bookName = resultSet.getString("book_name");
                String borrowDate = resultSet.getString("borrow_date");
                String returnDate = resultSet.getString("returned_date");
                String status = resultSet.getString("status");
                String memberId = resultSet.getString("member_id");
                String bookId = resultSet.getString("book_id");

                borrows.add(new Borrow(id, memberId, bookId, bookName, borrowDate, returnDate, status));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error while filtering data: " + e.getMessage());
        }

        return borrows;
    }

    public List<Borrow> filter() {
        List<Borrow> borrowList = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM borrow");
        List<String> params = new ArrayList<>();

        // Xây dựng câu truy vấn dựa trên đầu vào
        if (!year.getText().isEmpty()) {
            sqlBuilder.append(" WHERE YEAR(borrow_date) = ?");
            params.add(year.getText());

            if (!month.getText().isEmpty()) {
                sqlBuilder.append(" AND MONTH(borrow_date) = ?");
                params.add(month.getText());

                if (!day.getText().isEmpty()) {
                    sqlBuilder.append(" AND DAY(borrow_date) = ?");
                    params.add(day.getText());
                }
            }
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Information", "Please enter a year.");
            return borrowList; // Trả về danh sách rỗng nếu thiếu năm
        }

        // Kết nối cơ sở dữ liệu và thực thi truy vấn
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlBuilder.toString())) {

            // Gán tham số cho câu lệnh SQL
            for (int i = 0; i < params.size(); i++) {
                statement.setString(i + 1, params.get(i));
            }

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String id = resultSet.getString("id");
                String bookName = resultSet.getString("book_name");
                String borrowDate = resultSet.getString("borrow_date");
                String returnDate = resultSet.getString("returned_date");
                String status = resultSet.getString("status");
                String memberId = resultSet.getString("member_id");
                String bookId = resultSet.getString("book_id");

                borrowList.add(new Borrow(id, memberId, bookId, bookName, borrowDate, returnDate, status));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error while filtering data: " + e.getMessage());
        }

        return borrowList;
    }

    public static List<Borrow> getBorrow() {
        List<Borrow> borrows = new ArrayList<>();
        String sql = "SELECT * FROM borrow ORDER BY borrow_date DESC";
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                String ID = resultSet.getString("id");
                String name = resultSet.getString("book_name");
                String borrowDate = resultSet.getString("borrow_date");
                String returnDate = resultSet.getString("returned_date");
                String status = resultSet.getString("status");
                String member_id = resultSet.getString("member_id");
                String book_id = resultSet.getString("book_id");

                borrows.add(new Borrow(ID, member_id, book_id, name, borrowDate, returnDate, status));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return borrows;
    }

    @FXML
    private void addBorrow() {
        pane1.setVisible(false);
        pane2.setVisible(true);

        year.setText("");
        month.setText("");
        day.setText("");
    }

    private void resetData() {
        info_id.setText("");
        info_birthday.setText("");
        info_email.setText("");
        info_phone.setText("");
        info_name.setText("");
        info_gender.setText("");

        bookname.setText("");

        member_field.setText("");
        book_field.setText("");
    }

    @FXML
    private void gotoHome(MouseEvent event) {
        MenuEvent.gotoHome(event);
    }

    @FXML
    private void gotoBook(MouseEvent event) {
        MenuEvent.gotoBook(event);
    }

    @FXML
    private void gotoMember(MouseEvent event) {
        MenuEvent.gotoMember(event);
    }

    @FXML
    private void gotoBorrow(MouseEvent event) {
        MenuEvent.gotoBorrow(event);
    }

    @FXML
    private void gotoIncome(MouseEvent event) {
        MenuEvent.gotoIncome(event);
    }

    @FXML
    private void gotoLogin(MouseEvent event) {
        MenuEvent.gotoLogin(event);
    }

}
