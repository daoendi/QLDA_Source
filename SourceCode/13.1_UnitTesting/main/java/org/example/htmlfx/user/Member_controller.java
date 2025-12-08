package org.example.htmlfx.user;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.util.Callback;
import org.example.htmlfx.toolkits.DatabaseConnection;
import org.example.htmlfx.toolkits.MenuEvent;
import org.example.htmlfx.toolkits.SearchBar;
import org.example.htmlfx.SwitchScene;
import org.example.htmlfx.borrow.Borrow;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static org.example.htmlfx.toolkits.Alert.showAlert;

public class Member_controller implements Initializable {
    @FXML
    private TableView<Member> tableView;

    @FXML
    private TableColumn<Member, String> EmailColumn;

    @FXML
    private TableColumn<Member, String> PhoneColumn;

    @FXML
    private TableColumn<Member, String> NameColumn;

    @FXML
    private TableColumn<Member, String> IDColumn;

    @FXML
    private TableColumn<Member, String> GenderColumn;

    @FXML
    private TableView<Borrow> tableInfo;

    @FXML
    private TableColumn<Borrow, String> BorrowedIDColumn;

    @FXML
    private TableColumn<Borrow, String> BooknameColumn;

    @FXML
    private TableColumn<Borrow, String> BorrowedColumn;

    @FXML
    private TableColumn<Borrow, String> ReturnedColumn;

    @FXML
    private TableColumn<Borrow, String> StatusColumn;

    @FXML
    private ListView<String> listView;

    @FXML
    private TextField searchID;

    @FXML
    private Pane pane1;

    @FXML
    private Pane pane2;

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
    private ImageView image;

    @FXML
    private Text num_of_mem;

    private Member selectedMember = new Member();

    private static Member currentMember = new Member();

    private static Member selectedMemberInSuggestionList = new Member();

    public Member getSelectedMember() {
        return selectedMember;
    }

    public Member getSelectedMemberInSuggestionList() {
        return selectedMemberInSuggestionList;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // khoi tao cho tableView
        EmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        PhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        IDColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        NameColumn.setCellValueFactory(new PropertyValueFactory<>("lastname"));
        GenderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));

        ObservableList<Member> members = FXCollections.observableArrayList(Member_controller.getMember());
        if (members.isEmpty()) {
            System.out.println("No members found in the database.");
        } else {
            tableView.setItems(members);
            total_members();
        }

        //khoi tao cho tableInfo
        BorrowedIDColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        BooknameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        BorrowedColumn.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        ReturnedColumn.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        StatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        Callback<TableColumn<Member, String>, TableCell<Member, String>> cellFactory = new Callback<TableColumn<Member, String>, TableCell<Member, String>>() {
            @Override
            public TableCell<Member, String> call(TableColumn<Member, String> param) {
                return new TableCell<Member, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty); if (item == null || empty) {
                            setText(null);
                        } else {
                            setText(item);
                            setStyle("-fx-alignment: CENTER;");
                        }
                    }
                };
            }
        };

        IDColumn.setCellFactory(cellFactory);
        PhoneColumn.setCellFactory(cellFactory);

        // Thêm sự kiện nhấn đúp chuột vào TableView
        tableView.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                Member selected = tableView.getSelectionModel().getSelectedItem();
                if (selected != null) { // Thêm kiểm tra
                    selectedMember = selected;
                    ObservableList<Borrow> temps = FXCollections.observableArrayList(Member_controller.getTemp(selected.getId()));
                    tableInfo.setItems(temps);
                    handleDoubleClick(selected);
                }
            }
        });

        SearchBar search = new SearchBar();
        search.setupSearchFieldForDatabase(searchID, listView, this);

        pane1.setVisible(true);
        pane2.setVisible(false);
    }

    private void setup(Member member) {
        info_id.setText(member.getId());
        info_name.setText(member.getFirstname() + ' ' + member.getLastname());
        info_phone.setText(member.getPhone());
        info_email.setText(member.getEmail());
        info_gender.setText(member.getGender());
        info_birthday.setText(member.getBirthday());

        String path = getClass().getResource(member.getImage()).toExternalForm();
        Image temp = new Image(path);
        image.setImage(temp);
    }

    private void handleDoubleClick(Member member) {
        setup(member);

        pane1.setVisible(false);
        pane2.setVisible(true);
    }

    public void addMember(ActionEvent event) throws IOException {
        SwitchScene sw = new SwitchScene();
        sw.openNewScene(event, "user/Member_Add.fxml", this);
    }

    public void updateTableView() {
        ObservableList<Member> members = FXCollections.observableArrayList(Member_controller.getMember());
        tableView.setItems(members);
        total_members();
    }

    public static List<Member> getMember() {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM members";
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                String id = resultSet.getString("member_id");
                String firstName = resultSet.getString("firstName");
                String lastName = resultSet.getString("lastName");
                String gender = resultSet.getString("gender");
                String birthday = resultSet.getString("birth");
                String email = resultSet.getString("email");
                String phone = resultSet.getString("phone");
                String image = resultSet.getString("image");

                members.add((new Member(id, firstName, lastName, gender, birthday, email, phone, image)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    public static List<Borrow> getTemp(String id) {
        List<Borrow> temps = new ArrayList<>();
        String sql = "SELECT * FROM borrow WHERE member_id = ? ORDER BY borrow_date DESC";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id); // Gán giá trị tham số
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String ID = resultSet.getString("id");
                String name = resultSet.getString("book_name");
                String borrowDate = resultSet.getString("borrow_date");
                String returnDate = resultSet.getString("returned_date");
                String status = resultSet.getString("status");

                temps.add(new Borrow(ID, name, borrowDate, returnDate, status));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return temps;
    }

    public void deleteMember(ActionEvent event) throws IOException {
        SwitchScene sw = new SwitchScene();
        sw.openNewScene(event, "user/Member_Delete.fxml", this);
    }

    public void back() throws IOException {
        pane1.setVisible(true);
        pane2.setVisible(false);
    }

    public void editMember(ActionEvent event) throws IOException {
        currentMember = selectedMember;
        SwitchScene sw = new SwitchScene();
        sw.openNewScene(event, "user/Member_Edit.fxml", this);
    }

    public static Member getCurrentMember() {
        return currentMember;
    }

    public void updatePane2(Member updatedMember) {
        setup(updatedMember);
    }

    public void selectItemInSgList() {
        Member member = new Member();
        String sql = "SELECT * FROM members WHERE member_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement selectMemberStatement = connection.prepareStatement(sql)) {

            selectMemberStatement.setString(1, searchID.getText()); // Gán giá trị cho tham số truy vấn
            ResultSet resultSet = selectMemberStatement.executeQuery(); // Thực hiện truy vấn và nhận kết quả

            if (resultSet.next()) { // Kiểm tra nếu có kết quả
                member.setId(resultSet.getString("member_id"));
                member.setEmail(resultSet.getString("email"));
                member.setPhone(resultSet.getString("phone"));
                member.setGender(resultSet.getString("gender"));
                member.setBirthday(resultSet.getString("birth"));
                member.setFirstname(resultSet.getString("firstname"));
                member.setLastname(resultSet.getString("lastname"));
                member.setImage(resultSet.getString("image"));

                selectedMember = member;
                currentMember = selectedMember;
                handleDoubleClick(member);
                ObservableList<Borrow> temps = FXCollections.observableArrayList(Member_controller.getTemp(searchID.getText()));
                tableInfo.setItems(temps);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
        }
    }

    public void total_members() {
        String sql = "SELECT COUNT(member_id) as total FROM members";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement count = connection.prepareStatement(sql)) {

            ResultSet resultSet = count.executeQuery(); // Thực hiện truy vấn và nhận kết quả

            resultSet.next();
            num_of_mem.setText(resultSet.getString("total") + " members");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
        }
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