package org.example.htmlfx.income;

public class Payment {
    private String id;
    private String member_id;
    private String book_id;
    private String payment;
    private String order_date;

    public Payment() {
    }

    public Payment(String id, String member_id, String book_id, String payment, String order_date) {
        this.id = id;
        this.member_id = member_id;
        this.book_id = book_id;
        this.payment = payment;
        this.order_date = order_date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBook_id() {
        return book_id;
    }

    public void setBook_id(String book_id) {
        this.book_id = book_id;
    }

    public String getMember_id() {
        return member_id;
    }

    public void setMember_id(String member_id) {
        this.member_id = member_id;
    }

    public String getPayment() {
        return payment;
    }

    public void setPrice(String payment) {
        this.payment = payment;
    }

    public String getOrder_date() {
        return order_date;
    }

    public void setOrder_date(String order_date) {
        this.order_date = order_date;
    }
}
