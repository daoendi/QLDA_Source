package org.example.htmlfx.borrow;

public class Borrow {
    private String id;
    private String member_id;
    private String book_id;
    private String name;
    private String borrowDate;
    private String returnDate;
    private String status;

    public Borrow() {}

    public Borrow(String id, String name, String borrowDate, String returnDate, String status) {
        this.id = id;
        this.name = name;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
        this.status = status;
    }

    public Borrow(String id, String member_id, String book_id, String name, String borrowDate, String returnDate, String status) {
        this.id = id;
        this.member_id = member_id;
        this.book_id = book_id;
        this.name = name;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(String borrowDate) {
        this.borrowDate = borrowDate;
    }

    public String getReturnDate() {
        return returnDate;
    }

    public void setReturnTime(String returnTime) {
        this.returnDate = returnTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMember_id() {
        return member_id;
    }

    public void setMember_id(String member_id) {
        this.member_id = member_id;
    }

    public String getBook_id() {
        return book_id;
    }

    public void setBook_id(String book_id) {
        this.book_id = book_id;
    }
}
