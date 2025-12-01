package org.example.htmlfx.dashboard;

import javafx.animation.KeyFrame;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.htmlfx.SceneController;
import org.example.htmlfx.SwitchScene;
import org.example.htmlfx.borrow.Borrow_controller;
import org.example.htmlfx.toolkits.DatabaseConnection;
import javafx.scene.control.ListView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import org.example.htmlfx.toolkits.MenuEvent;
import org.example.htmlfx.toolkits.Music;
import org.example.htmlfx.user.Admin;
import org.example.htmlfx.user.Member;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class DashboardControl {
    @FXML
    private TableView<MostBorrowed> tableView;
    @FXML
    private TableColumn<MostBorrowed, Integer> id;
    @FXML
    private TableColumn<MostBorrowed, String> name;
    @FXML
    private TableColumn<MostBorrowed, String> borrowedTimes;
    @FXML
    private LineChart<?, ?> lineChart;
    @FXML
    private Label getIncome;
    @FXML
    AnchorPane toolbar_Pane;
    @FXML
    private ListView<Notification> notificationListView;
    @FXML
    private ImageView settingButton;
    @FXML
    private Label getBorrow;
    @FXML
    private ImageView image;
    @FXML
    private ImageView bell;

    private Music music;

    private Notification_Service notificationService = new Notification_Service();

    @FXML
    public void initialize() {
        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        borrowedTimes.setCellValueFactory(new PropertyValueFactory<>("borrowedTimes"));

        ObservableList<MostBorrowed> data = FXCollections.observableArrayList(DashboardControl.getBorrowed());
        tableView.setItems(data);
        setLineChart();
        setIncome();
        setBorrow();
        music = org.example.htmlfx.SceneController.getMusic();
        if (music == null) {
            music = new Music();
            org.example.htmlfx.SceneController.setMusic(music);
        }
        notificationService.start();
        notificationListView.setItems(FXCollections.observableArrayList(notificationService.getNotifications()));
        notificationListView.setVisible(false);
        if(notificationService.getNotifications().isEmpty()) {bellRing();}

        String path = getClass().getResource(SceneController.getAdmin().getImage()).toExternalForm();
        Image temp = new Image(path);
        image.setImage(temp);
    }

    public void update(Admin admin) {
        SceneController.setAdmin(admin);
        String path = getClass().getResource(SceneController.getAdmin().getImage()).toExternalForm();
        Image temp = new Image(path);
        image.setImage(temp);
    }

    @FXML
    private void checkNotification() {
        // Kiểm tra trạng thái hiện tại của ListView
        if (notificationListView.isVisible()) {
            // Nếu ListView đang hiển thị, ẩn nó đi
            notificationListView.setVisible(false);
        } else {
            // Nếu ListView đang bị ẩn, hiển thị nó và cập nhật nội dung
            ObservableList<Notification> notifications = FXCollections.observableArrayList(notificationService.getNotifications());
            notificationListView.setItems(notifications);
            notificationListView.setVisible(true);
        }
    }

    @FXML
    private void info_admin(MouseEvent event) throws IOException {
        SwitchScene sw = new SwitchScene();
        sw.openNewScene(event, "user/Admin_Edit.fxml", this);
    }

    public static List<MostBorrowed> getBorrowed() {
        List<MostBorrowed> mostBorrowed = new ArrayList<>();
        // Sửa câu truy vấn để lấy 10 cuốn sách có time_of_borrow cao nhất
        String sql = "SELECT * FROM books ORDER BY time_of_borrow DESC LIMIT 10";
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                String id = resultSet.getString("book_id");
                String name = resultSet.getString("book_name");
                String borrowedTimes = resultSet.getString("time_of_borrow");
                mostBorrowed.add(new MostBorrowed(id, name, borrowedTimes));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mostBorrowed;
    }

    @FXML
    private void setLineChart() {
        XYChart.Series series = new XYChart.Series<>();
        series.setName("Borrow Count");
        String query = """
                    SELECT 
                        DAYNAME(DATE(borrow_date)) AS day_name, 
                        COUNT(*) AS borrow_count
                    FROM borrow
                    WHERE borrow_date BETWEEN DATE_SUB(CURDATE(), INTERVAL 6 DAY) AND CURDATE()
                    GROUP BY borrow_date
                    ORDER BY DATE(borrow_date);
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            // Duyệt qua ResultSet để thêm dữ liệu vào series
            while (rs.next()) {
                String dayName = rs.getString("day_name"); // Tên thứ
                int borrowCount = rs.getInt("borrow_count"); // Số lượt mượn

                // Thêm dữ liệu vào series
                series.getData().add(new XYChart.Data<>(dayName, borrowCount));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Thêm series vào LineChart
        lineChart.getData().clear(); // Xóa dữ liệu cũ (nếu có)
        lineChart.getData().add(series);
    }

    private void setIncome() {
        double totalPayment = 0.0;
        String sql = "SELECT SUM(price) AS total_payment FROM payment";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            if (resultSet.next()) {
                totalPayment = resultSet.getDouble("total_payment");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi truy vấn tổng giá trị payment: " + e.getMessage());
            e.printStackTrace();
        }
        getIncome.setText(String.valueOf(totalPayment) + " VND");
    }

    private void setBorrow() {
        String sql = "SELECT COUNT(*) AS borrow_count FROM borrow WHERE borrow_date = CURDATE()";
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            if (resultSet.next()) {
                getBorrow.setText(resultSet.getString("borrow_count"));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi truy vấn số lượt mượn sách hôm nay: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void SettingHandle(MouseEvent mouseEvent) {
        // Tạo nút bật/tắt nhạc
        Button togglePlayButton = new Button("▶ Play");
        togglePlayButton.setStyle("-fx-font-size: 16px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10px;");
        togglePlayButton.setOnAction(e -> {
            if (music.isPlaying()) {
                music.pause();
                togglePlayButton.setText("▶ Play");
                togglePlayButton.setStyle("-fx-font-size: 16px; -fx-background-color: #4CAF50; -fx-text-fill: white;");
            } else {
                music.play();
                togglePlayButton.setText("⏸ Pause");
                togglePlayButton.setStyle("-fx-font-size: 16px; -fx-background-color: #44a5ff; -fx-text-fill: white;");
            }
        });

        // Nút Tắt tiếng
        Button muteButton = new Button("🔇 Mute");
        muteButton.setStyle("-fx-font-size: 16px; -fx-background-color: #44a5ff; -fx-text-fill: white; -fx-padding: 10px;");
        muteButton.setOnAction(e -> {
            if (music.getVolume() > 0) {
                music.setVolume(0);
                muteButton.setText("🔊 Unmute");
            } else {
                music.setVolume(0.5); // Đặt âm lượng về mức 50% sau khi bật lại
                muteButton.setText("🔇 Mute");
            }
        });

        // Tạo slider điều chỉnh âm lượng
        Label volumeLabel = new Label("Volume: 50%");
        volumeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
        Slider volumeSlider = new Slider(0, 1, 0.5);
        volumeSlider.setShowTickLabels(true);
        volumeSlider.setShowTickMarks(true);
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            music.setVolume(newValue.doubleValue());
            volumeLabel.setText("Volume: " + Math.round(newValue.doubleValue() * 100) + "%");
        });

        // Tạo nhãn tiêu đề
        Label titleLabel = new Label("🎵 Music Player 🎵");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");

        // Căn chỉnh bố cục
        VBox root = new VBox(20, titleLabel, togglePlayButton, muteButton, volumeLabel, volumeSlider);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #ffffff, #f1f1f1); -fx-border-color: #ccc; -fx-border-width: 2px; -fx-border-radius: 10px; -fx-background-radius: 10px;");

        // Tạo Scene và thiết lập Stage
        Scene scene = new Scene(root, 400, 350);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Music Control Settings");
        stage.setOnCloseRequest(e -> music.dispose());
        stage.show();
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

    @FXML
    private void gotoBorrow_temp(MouseEvent event) {
        Borrow_controller.check = false;
        MenuEvent.gotoBorrow(event);
    }

    private void shakeBell() {
        // Tạo hiệu ứng lắc
        RotateTransition shake = new RotateTransition(Duration.millis(100), bell);
        shake.setFromAngle(-10); // Góc bắt đầu lắc
        shake.setToAngle(10);    // Góc kết thúc lắc
        shake.setCycleCount(10);  // 3 lần lắc (6 chu kỳ)
        shake.setAutoReverse(true); // Tự động đảo ngược góc lắc
        shake.play();

    }
    private void stopShake() {
        // Dừng hiệu ứng lắc và chờ 1 giây
        bell.setRotate(0); // Khôi phục góc về 0
    }
    public void bellRing() {
        // Tạo Timeline cho lắc chuông
        Timeline timeline = new Timeline(
                // Lắc qua lại trong 3 giây
                new KeyFrame(Duration.millis(0), e -> shakeBell()),  // Bắt đầu lắc ngay lập tức
                new KeyFrame(Duration.millis(1500), e -> stopShake()) // Dừng lắc sau 3 giây
        );

        // Sau 3 giây, nghỉ 1 giây và tiếp tục lắc
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
}