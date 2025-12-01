package org.example.htmlfx.book;


import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.htmlfx.toolkits.DatabaseConnection;
import org.example.htmlfx.toolkits.MenuEvent;
import org.example.htmlfx.toolkits.SearchBar;
import org.json.JSONArray;
import org.json.JSONObject;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import javafx.geometry.Insets;

import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

import static org.example.htmlfx.toolkits.Alert.showAlert;

public class BookController implements Initializable {
    public String link = "https://www.facebook.com/kieuvantuyen01";
    public int amount = 0;
    List<Book> bookCase = new ArrayList<>();
    private final BookCache bookCache = new BookCache();
    @FXML
    private GridPane caseBook;

    @FXML
    private TextArea descriptionBook;

    @FXML
    private Label detailsBook;

    @FXML
    private Button downloadButton;

    @FXML
    private ImageView imageBook;

    @FXML
    private Label nameBook;

    @FXML
    private Button returnButton;


    @FXML
    private ScrollPane scrollBook;

    @FXML
    private AnchorPane showBook;

    @FXML
    private Button showQR;

    @FXML
    TextField textField;
    @FXML
    ListView<String> viewList;
    @FXML
    Label stocklabel;
    @FXML
    TextField amountBook;
    @FXML
    Label statusLabel;

    private MyClicked myClicked;
    SearchBar searchBar = new SearchBar();
    private StringProperty querry = new SimpleStringProperty();
    public Book recentlybook;

    public StringProperty querryProperty() {
        return querry;
    }

    public String getQuerry() {
        return querry.get();
    }

    public void setQuerry(String querry) {
        this.querry.set(querry);
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        int column = 0;
        int row = 1;
        bookCase = new ArrayList<>();

        // Lắng nghe sự thay đổi của query
        querryProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.trim().isEmpty()) {
                updateBooksCF(newValue.trim()); // Gọi phương thức cập nhật lại dữ liệu sách
            }
        });

        // Xử lý khi nhấn Enter trên textField
        textField.setOnAction(event -> {
            String query = textField.getText().trim();
            textField.setText("");
            if (!query.isEmpty()) {
                updateBooksCF(query);
            }
        });

        // Tạo callback khi nhấn vào sách
        myClicked = new MyClicked() {
            @Override
            public void myClicked(Book book) throws IOException, WriterException {
                setDataShowBook(book);
                showBook.setVisible(true);
                scrollBook.setVisible(false);
                link = book.getPreviewLink();
                quantity_in_Stock_Book(book.getAuthors());
                recentlybook = book;
                System.out.println(recentlybook.getbookmark());
                quantity_in_Stock_Book(recentlybook.getbookmark());
            }
        };

        // Tải dữ liệu mặc định (nếu cần)
        updateBooksCF("Naruto"); // Từ khóa mặc định

        // Cài đặt thanh tìm kiếm
        searchBar.setupSearchField(textField, viewList);
    }
    private void updateBooksCF(String query) {
        // Kiểm tra cache trước
        if (bookCache.contains(query)) {
            List<Book> cachedBooks = bookCache.get(query);
            loadBooksToUI(cachedBooks);
            statusLabel.setText("Dữ liệu được tải từ cache.");
            return;
        }

        // Hiển thị thông báo đang tải
        statusLabel.setText("Đang tải dữ liệu...");

        // Gọi API bất đồng bộ
        fetchBooksFromAPIAsync(query, 15)
                .thenAccept(books -> {
                    Platform.runLater(() -> {
                        if (books.isEmpty()) {
                            statusLabel.setText("Không tìm thấy sách phù hợp.");
                            return;
                        }

                        // Lưu vào cache
                        bookCache.put(query, books);

                        // Hiển thị sách
                        loadBooksToUI(books);

                        // Cập nhật trạng thái
                        statusLabel.setText("Dữ liệu đã được tải thành công.");
                    });
                })
                .exceptionally(ex -> {
                    // Xử lý lỗi
                    Platform.runLater(() -> {
                        statusLabel.setText("Lỗi khi tải dữ liệu: " + ex.getMessage());
                    });
                    return null;
                });
    }

    private void loadBooksToUI(List<Book> books) {
        // Xóa các sách cũ trong GridPane
        caseBook.getChildren().clear();
        int column = 0;
        int row = 1;

        // Hiển thị sách mới
        try {
            for (Book book : books) {
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("BookCase.fxml"));
                VBox bookCaseBox = fxmlLoader.load();

                // Gán dữ liệu cho từng sách
                BookCaseController bookCaseController = fxmlLoader.getController();
                bookCaseController.setData(book, myClicked);
                bookCaseBox.setUserData(book);

                // Thêm vào GridPane
                if (column == 5) {
                    column = 0;
                    ++row;
                }
                caseBook.add(bookCaseBox, column++, row);
                GridPane.setMargin(bookCaseBox, new Insets(10));
            }
        } catch (IOException e) {
            System.out.println("Error loading FXML for bookcase: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @FXML
    void backtoSearchBook(MouseEvent event) {
        showBook.setVisible(false);
        scrollBook.setVisible(true);
    }


    public void setDataShowBook(Book book) {
        if (book.getImageLink() != null && !book.getImageLink().isEmpty()) {
            imageBook.setImage(new Image(book.getImageLink()));
        } else {
            imageBook.setImage(new Image("/path/to/default/image.jpg")); // Đường dẫn ảnh mặc định
        }

        nameBook.setText("Title: " + book.getTitle());
        descriptionBook.setText(book.getDescription() != null ? book.getDescription() : "No description available.");
        descriptionBook.setWrapText(true);
        descriptionBook.setEditable(false);

        detailsBook.setText(
                "Author(s): " + book.getAuthors() + "\n" +
                        "Publisher: " + book.getPublisher() + "\n" +
                        "Published Date: " + book.getPublicationDate() + "\n" +
                        "Language: " + book.getLanguage() + "\n" +
                        "Categories: " + book.getSubject() + "\n" +
                        "Page Count: " + book.getNumberOfPages()
        );
        downloadButton.setOnAction(e -> downloadBook(book));

    }

    private void downloadBook(Book book) {
        if (book.getPreviewLink() != null && !book.getPreviewLink().isEmpty()) {
            try {
                Desktop.getDesktop().browse(URI.create(book.getPreviewLink()));
            } catch (Exception e) {
                System.out.println("Error downloading book: " + e.getMessage());
            }
        } else {
            supershowAlert("Download Not Available", "This book does not have a download link.");
        }
    }

    private void searchBooksAndUpdateUI(String keyword, int maxResults, ListView<Book> bookListView, Label statusLabel) {
        // Hiển thị trạng thái đang tải
        statusLabel.setText("Đang tải dữ liệu...");

        fetchBooksFromAPIAsync(keyword, maxResults)
                .thenAccept(books -> {
                    // Cập nhật giao diện trên JavaFX Application Thread
                    Platform.runLater(() -> {
                        // Hiển thị danh sách sách vào ListView
                        bookListView.getItems().clear();
                        bookListView.getItems().addAll(books);

                        // Cập nhật trạng thái
                        if (books.isEmpty()) {
                            statusLabel.setText("Không tìm thấy kết quả.");
                        } else {
                            statusLabel.setText("Tìm thấy " + books.size() + " kết quả.");
                        }
                    });
                })
                .exceptionally(ex -> {
                    // Xử lý lỗi và cập nhật giao diện
                    Platform.runLater(() -> {
                        statusLabel.setText("Có lỗi xảy ra: " + ex.getMessage());
                    });
                    return null;
                });
    }


    private static final String GOOGLE_BOOKS_API_BASE_URL = "https://www.googleapis.com/books/v1/volumes";

    private CompletableFuture<List<Book>> fetchBooksFromAPIAsync(String keyword, int maxResults) {
        return CompletableFuture.supplyAsync(() -> {
            List<Book> books = new ArrayList<>();
            try {
                String query = "?q=" + URLEncoder.encode(keyword, StandardCharsets.UTF_8) + "&maxResults=" + maxResults;
                URI uri = URI.create(GOOGLE_BOOKS_API_BASE_URL + query);

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(uri)
                        .GET()
                        .build();

                HttpResponse<String> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .join();

                if (response.statusCode() == 200) {
                    JSONObject jsonResponse = new JSONObject(response.body());
                    JSONArray items = jsonResponse.optJSONArray("items");

                    if (items != null) {
                        for (int i = 0; i < Math.min(maxResults, items.length()); i++) {
                            JSONObject bookJson = items.getJSONObject(i);
                            JSONObject volumeInfo = bookJson.optJSONObject("volumeInfo");
                            JSONObject accessInfo = bookJson.optJSONObject("accessInfo");

                            if (volumeInfo != null) {
                                String id = bookJson.optString("id", "Unknown ID");
                                String title = volumeInfo.optString("title", "Unknown Title");
                                String authors = volumeInfo.optJSONArray("authors") != null
                                        ? volumeInfo.optJSONArray("authors").join(", ").replace("\"", "")
                                        : "Unknown Author";
                                String publisher = volumeInfo.optString("publisher", "Unknown Publisher");
                                String publishedDate = volumeInfo.optString("publishedDate", "Unknown Date");
                                String language = volumeInfo.optString("language", "Unknown Language");
                                int pageCount = volumeInfo.optInt("pageCount", 0);
                                String printType = volumeInfo.optString("printType", "Unknown Print Type");
                                String categories = volumeInfo.optJSONArray("categories") != null
                                        ? volumeInfo.optJSONArray("categories").join(", ").replace("\"", "")
                                        : "N/A";
                                String description = volumeInfo.optString("description", "No description available.");

                                String imageLink = "";
                                JSONObject imageLinks = volumeInfo.optJSONObject("imageLinks");
                                if (imageLinks != null) {
                                    imageLink = imageLinks.optString("thumbnail", "");
                                }

                                String previewLink = volumeInfo.optString("previewLink", "");
                                String downloadLink = "";
                                if (accessInfo != null) {
                                    JSONObject pdfInfo = accessInfo.optJSONObject("pdf");
                                    if (pdfInfo != null) {
                                        downloadLink = pdfInfo.optString("downloadLink", "");
                                    }
                                }

                                Book book = new Book(
                                        id, title, categories, publisher, publishedDate, language, pageCount,
                                        printType, authors, imageLink, downloadLink, previewLink, description,
                                        pageCount / 6, (double) pageCount / 4
                                );
                                books.add(book);
                            }
                        }
                    }
                } else {
                    System.err.println("Error fetching data: HTTP " + response.statusCode() + " - " + response.body());
                }
            } catch (Exception e) {
                System.err.println("Error fetching data from Google Books API: " + e.getMessage());
                e.printStackTrace();
            }
            return books;
        });
    }


    private void supershowAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    public void handleSearch(MouseEvent actionEvent) {

        viewList.setVisible(false);
        String query = textField.getText().trim();
        textField.clear(); // Xóa textField sau khi tìm kiếm// Lấy giá trị nhập vào từ textField
        if (!query.isEmpty()) {
            querry.set(query);  // Cập nhật querry nếu có giá trị
            updateBooksCF(query); // Gọi updateBooks để tải lại danh sách sách
        } else {
            supershowAlert("Input Error", "Please enter a search term.");
        }

    }

    private WritableImage generateQRCodeImage(String text, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        // Tạo BitMatrix từ nội dung
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);

        // Chuyển BitMatrix thành BufferedImage
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int color = (bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF); // Đen hoặc trắng
                bufferedImage.setRGB(x, y, color);
            }
        }

        // Chuyển BufferedImage thành WritableImage để sử dụng trong JavaFX
        WritableImage writableImage = new WritableImage(width, height);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                writableImage.getPixelWriter().setArgb(x, y, bufferedImage.getRGB(x, y));
            }
        }
        return writableImage;
    }

    public void showQRClicked(MouseEvent mouseEvent) throws IOException, WriterException {
        Stage primaryStage = new Stage();
        ImageView qrCodeView = new ImageView(generateQRCodeImage(link, 300, 300));

        // Tạo button để đóng ứng dụng
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> primaryStage.close()); // Đóng màn hình khi nhấn nút

        // Hiển thị QR code và button
        VBox root = new VBox(qrCodeView, closeButton);
        root.setSpacing(10); // Khoảng cách giữa QR code và nút
        root.setAlignment(Pos.CENTER); // Căn giữa QR code và nút
        Scene scene = new Scene(root, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("QR Code");
        primaryStage.show();
    }

    public void quantity_in_Stock_Book(String bookmark) {
        String query = "SELECT remaining_quantity FROM books WHERE bookmark = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Truyền tham số vào câu truy vấn
            statement.setString(1, bookmark);

            // Thực hiện truy vấn
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                // Nếu tìm thấy sách, in ra số lượng
                int quantity = resultSet.getInt("remaining_quantity");
                stocklabel.setText("Book is available. Quantity: " + quantity);
            } else {
                // Nếu không tìm thấy sách
                stocklabel.setText("Book is not available in the library.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void addAmountBooktoLibrary(MouseEvent mouseEvent) {
        amount = Integer.parseInt(amountBook.getText());
        Connection connection = DatabaseConnection.getConnection();
        addBookToDatabase(connection, recentlybook, amount);
        showAlert(Alert.AlertType.INFORMATION, "Notice", "Add Book to Library Successfully.");
        amountBook.setText("");
        quantity_in_Stock_Book(recentlybook.getbookmark());

    }


    public void addBookToDatabase(Connection connection, Book book, int amount) {
        String checkExistQuery = "SELECT quantity_in_store FROM books WHERE bookmark = ?";
        String updateQuantityQuery = "UPDATE books SET quantity_in_store = quantity_in_store + ? WHERE bookmark = ?";
        String insertBookQuery = "INSERT INTO books (bookmark,book_name, book_author, price,time_of_borrow,quantity_in_store,borrowing) VALUES (?, ?, ?, ?,?,?,?)";

        try {
            // Kiểm tra xem sách đã có trong cơ sở dữ liệu chưa
            PreparedStatement checkStmt = connection.prepareStatement(checkExistQuery);
            checkStmt.setString(1, book.getbookmark());
            ResultSet resultSet = checkStmt.executeQuery();

            if (resultSet.next()) {
                // Nếu sách đã có, tăng số lượng
                int currentQuantity = resultSet.getInt("quantity_in_store");
                PreparedStatement updateStmt = connection.prepareStatement(updateQuantityQuery);
                updateStmt.setInt(1, amount); // Thêm số lượng vào hiện tại
                updateStmt.setString(2, book.getbookmark());
                updateStmt.executeUpdate();
            } else {
                // Nếu sách chưa có, thêm sách mới vào cơ sở dữ liệu
                PreparedStatement insertStmt = connection.prepareStatement(insertBookQuery);
                insertStmt.setString(1, book.getbookmark());
                insertStmt.setString(2, book.getTitle());
                insertStmt.setString(3, book.getAuthors());
                insertStmt.setDouble(4, book.getPrice());
                insertStmt.setInt(5, 0);
                insertStmt.setInt(6, amount);
                insertStmt.setInt(7, 0);
                insertStmt.executeUpdate();
                System.out.println("Added new book: " + book.getbookmark());
            }

        } catch (SQLException e) {
            e.printStackTrace();
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