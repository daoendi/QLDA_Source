package org.example.htmlfx.user;

public class Member extends Users {

    private int totalBooksCheckedOut = 0;

    public Member() {
        super();
    }

    public Member(String id, String firstname, String lastname, String gender, String birthday, String email, String phone, String image) {
        super(id, firstname, lastname, gender, birthday, email, phone, image);
    }

    public Member(String id, String firstname, String lastname, String gender, String birthday, String email, String phone) {
        super(id, firstname, lastname, gender, birthday, email, phone);
    }

    public int getTotalBooksCheckedOut() {
        return totalBooksCheckedOut;
    }

    public void setTotalBooksCheckedOut(int totalBooksCheckedOut) {
        this.totalBooksCheckedOut = totalBooksCheckedOut;
    }

    public void addBorrowed() {

    }
}
