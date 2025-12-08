package org.example.htmlfx.book;


import com.google.zxing.WriterException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;

public class BookCaseController {

    @FXML
    private Label authorCase;

    @FXML
    private ImageView imageCase;

    @FXML
    private Label nameCase;

    private Book sbook;
    private MyClicked myClicked;
    public void setData(Book book, MyClicked myClicked) {
        sbook = book;
        this.myClicked = myClicked;
        if (book != null) {
            nameCase.setText(book.getTitle());
            authorCase.setText(book.getAuthors());

            if (book.getImageLink() != null && !book.getImageLink().isEmpty()) {
                imageCase.setImage(new Image(book.getImageLink()));
            } else {
                imageCase.setImage(new Image("/path/to/default/image.jpg")); // Ảnh mặc định nếu không có ảnh
            }
        } else {
            System.out.println("Book object is null");
        }
    }
    public String LinkQR() {
        return sbook.getDownloadLink();
    }
    public void click(javafx.scene.input.MouseEvent mouseEvent) throws IOException, WriterException {
        myClicked.myClicked(sbook);
    }
}

