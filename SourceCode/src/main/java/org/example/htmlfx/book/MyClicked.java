package org.example.htmlfx.book;

import com.google.zxing.WriterException;

import java.io.IOException;

public interface MyClicked {
    public void myClicked(Book book) throws IOException, WriterException;

}
