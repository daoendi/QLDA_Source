package org.example.htmlfx.toolkits;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.*;
import org.example.htmlfx.income.Income_Control;
import org.example.htmlfx.user.Member_controller;

public class SearchBar {

    private Timeline debounceTimeline;

    // Phương thức để xử lý TextField khi tìm kiếm sách
    public void setupSearchField(TextField searchField, ListView<String> suggestionList) {
        // Xử lý sự kiện khi người dùng nhập vào TextField
        searchField.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            // Hủy Timeline cũ nếu có, để tránh việc gọi API liên tục
            if (debounceTimeline != null) {
                debounceTimeline.stop();
            }

            String query = searchField.getText().trim();
            if (query.isEmpty()) {
                suggestionList.setVisible(false); // Ẩn khi không có văn bản
            } else {
                // Tạo một Timeline để trì hoãn việc gọi API
                debounceTimeline = new Timeline(new KeyFrame(Duration.millis(300), e -> {
                    // Sau 300ms, thực hiện tìm kiếm
                    fetchBookTitlesFromGoogleAsync(query, suggestionList);
                }));
                debounceTimeline.play(); // Bắt đầu Timeline
            }
        });

        suggestionList.setOnMouseClicked(event -> {
            String selectedItem = suggestionList.getSelectionModel().getSelectedItem();
            searchField.setText(selectedItem); // Điền vào TextField
            suggestionList.setVisible(false); // Ẩn danh sách gợi ý
        });
    }

    // Phương thức lấy danh sách sách từ Google Books API một cách bất đồng bộ
    private void fetchBookTitlesFromGoogleAsync(String query, ListView<String> suggestionList) {
        Task<List<String>> task = new Task<List<String>>() {
            @Override
            protected List<String> call() throws Exception {
                return fetchBookTitlesFromGoogle(query); // Gọi phương thức fetchBookTitlesFromGoogle trong nền
            }
        };

        task.setOnSucceeded(e -> {
            // Cập nhật UI khi nhận được kết quả
            List<String> suggestions = task.getValue();
            if (!suggestions.isEmpty()) {
                updateSuggestions(suggestions, suggestionList);
            } else {
                suggestionList.setVisible(false);
            }
        });

        task.setOnFailed(e -> {
            task.getException().printStackTrace();
        });

        // Chạy Task trong nền
        new Thread(task).start();
    }

    // Phương thức lấy danh sách sách từ Google Books API
    private List<String> fetchBookTitlesFromGoogle(String query) {
        List<String> bookTitles = new ArrayList<>();
        try {
            String urlString = "https://www.googleapis.com/books/v1/volumes?q=" + query.replace(" ", "+") + "&maxResults=10"; // Giới hạn kết quả
            HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            if (conn.getResponseCode() == 200) {
                InputStreamReader reader = new InputStreamReader(conn.getInputStream());
                JsonObject jsonResponse = JsonParser.parseReader(reader).getAsJsonObject();
                JsonArray items = jsonResponse.getAsJsonArray("items");

                if (items != null) {
                    for (int i = 0; i < items.size(); i++) {
                        JsonObject book = items.get(i).getAsJsonObject();
                        JsonObject volumeInfo = book.getAsJsonObject("volumeInfo");
                        String title = volumeInfo.has("title") ? volumeInfo.get("title").getAsString() : "No title";
                        bookTitles.add(title);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bookTitles;
    }


    public void setupSearchFieldForDatabase(TextField searchField, ListView<String> suggestionList, Member_controller controller) {
        // Xử lý sự kiện khi người dùng nhập vào TextField
        searchField.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            String query = searchField.getText().trim();
            if (query.isEmpty()) {
                suggestionList.setVisible(false); // Ẩn khi không có văn bản
            } else {
                List<String> suggestions = fetchMemberIDFromDatabase(query);
                if (!suggestions.isEmpty()) {
                    updateSuggestions(suggestions, suggestionList);
                } else {
                    suggestionList.setVisible(false);
                }
            }
        });

        // Chọn một gợi ý khi người dùng click vào
        suggestionList.setOnMouseClicked(event -> {
            String selectedItem = suggestionList.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                searchField.setText(selectedItem); // Điền vào TextField
                suggestionList.setVisible(false); // Ẩn danh sách gợi ý

                // Gọi phương thức selectItemInSgList trong controller
                controller.selectItemInSgList();
                searchField.setText("");
            }
        });
    }

    public void setupSearchFieldForDatabase(TextField searchField, ListView<String> suggestionList, Income_Control controller) {
        // Xử lý sự kiện khi người dùng nhập vào TextField
        searchField.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            String query = searchField.getText().trim();
            if (query.isEmpty()) {
                suggestionList.setVisible(false); // Ẩn khi không có văn bản
            } else {
                List<String> suggestions = fetchMemberIDFromDatabase(query);
                if (!suggestions.isEmpty()) {
                    updateSuggestions(suggestions, suggestionList);
                } else {
                    suggestionList.setVisible(false);
                }
            }
        });

        // Chọn một gợi ý khi người dùng click vào
        suggestionList.setOnMouseClicked(event -> {
            String selectedItem = suggestionList.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                searchField.setText(selectedItem); // Điền vào TextField
                suggestionList.setVisible(false); // Ẩn danh sách gợi ý

                // Gọi phương thức selectItemInSgList trong controller
                controller.selectItemInSgList();
                searchField.setText("");
            }
        });
    }

    private List<String> fetchMemberIDFromDatabase(String query) {
        List<String> memberID = new ArrayList<>();
        String sql = "SELECT member_id FROM members WHERE member_id LIKE '%" + query + "%'";
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                String ID = resultSet.getString("member_id");
                memberID.add(ID);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return memberID;
    }

    // Phương thức cập nhật danh sách gợi ý
    private void updateSuggestions(List<String> suggestions, ListView<String> suggestionList) {
        ObservableList<String> observableSuggestions = FXCollections.observableArrayList(suggestions);
        suggestionList.setItems(observableSuggestions);
        suggestionList.setVisible(true); // Hiển thị lại danh sách gợi ý
    }
}

