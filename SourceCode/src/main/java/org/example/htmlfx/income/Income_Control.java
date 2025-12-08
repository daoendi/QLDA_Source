package org.example.htmlfx.income;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import org.example.htmlfx.toolkits.DatabaseConnection;
import org.example.htmlfx.toolkits.MenuEvent;
import org.example.htmlfx.toolkits.SearchBar;

import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static org.example.htmlfx.toolkits.Alert.showAlert;

public class Income_Control implements Initializable {
    @FXML
    private HBox root;

    @FXML
    private AnchorPane toolbar_pane;

    @FXML
    private TableColumn<Payment, String> date;

    @FXML
    private TableColumn<Payment, String> id_book;

    @FXML
    private TableColumn<Payment, String> id_member;

    @FXML
    private TableColumn<Payment, String> payment;

    @FXML
    private TableColumn<Payment, String> id_purchase;

    @FXML
    private TableView<Payment> payment_table;

    @FXML
    private Pane pane1;

    @FXML
    private Pane pane2;

    @FXML
    private TextField searchID;

    @FXML
    private ListView<String> listView;

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

    @FXML
    private TextField quantity_field;

    @FXML
    private Text amount;

    private double value;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        id_purchase.setCellValueFactory(new PropertyValueFactory<>("id"));
        id_member.setCellValueFactory(new PropertyValueFactory<>("member_id"));
        id_book.setCellValueFactory(new PropertyValueFactory<>("book_id"));
        payment.setCellValueFactory(new PropertyValueFactory<>("payment"));
        date.setCellValueFactory(new PropertyValueFactory<>("order_date"));

        ObservableList<Payment> data= FXCollections.observableArrayList(Income_Control.getIncome());
        payment_table.setItems(data);

        SearchBar search = new SearchBar();
        search.setupSearchFieldForDatabase(searchID, listView, this);

        member_field.setOnAction(event -> getInfoMember());

        book_field.setOnAction(event -> getInfoBook());

        quantity_field.setOnAction(event -> getTotal());

        pane1.setVisible(true);
        pane2.setVisible(false);
    }

    public static List<Payment> getIncome() {
        List<Payment> payments = new ArrayList<>();
        // Sửa câu truy vấn để lấy 10 cuốn sách có time_of_borrow cao nhất
        String sql = "SELECT * FROM payment ORDER BY order_date DESC ";
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                String id = resultSet.getString("id");
                String memberid = resultSet.getString("member_id");
                String bookid = resultSet.getString("book_id");
                String payment = resultSet.getString("payment");
                String date = resultSet.getString("order_date");
                payments.add(new Payment(id, memberid, bookid, payment, date));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return payments;
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

    private void getTotal() {
        String total = quantity_field.getText();
        int temp = Integer.parseInt(total);
        double res = temp * value;
        amount.setText(String.valueOf(res) + "00 VND");
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
        String price = rs.getString("price");
        String remain = rs.getString("remaining_quantity");
        String res = "Name: " + name + "\nPrice: " + price + "00 VND" + "\n" + "Remaining: " + remain;
        bookname.setText(res);

        value = rs.getDouble("price");
    }

    public void selectItemInSgList() {

        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payment WHERE member_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {;
            statement.setString(1, searchID.getText());
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String id = resultSet.getString("id");
                String memberid = resultSet.getString("member_id");
                String bookid = resultSet.getString("book_id");
                String payment = resultSet.getString("price");
                String date = resultSet.getString("order_date");
                payments.add(new Payment(id, memberid, bookid,payment,date));
            }

            ObservableList<Payment> temps = FXCollections.observableArrayList(payments);
            payment_table.setItems(temps);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Payment> filter() {
        List<Payment> lists = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM payment");
        List<String> params = new ArrayList<>();

        // Xây dựng câu truy vấn dựa trên đầu vào
        if (!year.getText().isEmpty()) {
            sqlBuilder.append(" WHERE YEAR(order_date) = ?");
            params.add(year.getText());

            if (!month.getText().isEmpty()) {
                sqlBuilder.append(" AND MONTH(order_date) = ?");
                params.add(month.getText());

                if (!day.getText().isEmpty()) {
                    sqlBuilder.append(" AND DAY(order_date) = ?");
                    params.add(day.getText());
                }
            }
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Information", "Please enter a year.");
            return lists; // Trả về danh sách rỗng nếu thiếu năm
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
                String memberId = resultSet.getString("member_id");
                String bookId = resultSet.getString("book_id");
                String price = resultSet.getString("price");
                String order_date = resultSet.getString("order_date");

                lists.add(new Payment(id, memberId, bookId, price, order_date));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error while filtering data: " + e.getMessage());
        }

        return lists;
    }

    public void update_filter() {
        ObservableList<Payment> borrows = FXCollections.observableArrayList(filter());
        payment_table.setItems(borrows);
    }

    public void addPayment() {
        pane1.setVisible(false);
        pane2.setVisible(true);
    }

    @FXML
    private void back() {
        pane1.setVisible(true);
        pane2.setVisible(false);

        resetData();
    }

    @FXML
    private void save() {
        // Kiểm tra nếu các trường member_field hoặc book_field chưa được điền
        if (member_field.getText().isEmpty() || book_field.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please fill in both Member ID and Book ID fields.");
            return; // Dừng lại nếu thiếu thông tin
        }

        String sql = "INSERT INTO payment (member_id, book_id, quantity_of_order, order_date) VALUES (?, ?, ?, current_timestamp)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, member_field.getText());
            statement.setString(2, book_field.getText());
            statement.setString(3, quantity_field.getText());
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

        ObservableList<Payment> data= FXCollections.observableArrayList(Income_Control.getIncome());
        payment_table.setItems(data);

        pane1.setVisible(true);
        pane2.setVisible(false);

        resetData();
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
        quantity_field.setText("");
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
