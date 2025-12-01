package org.example.htmlfx.dashboard;

import java.time.LocalDate;

public class Notification {
    private String message;
    private LocalDate dueDate;

    public Notification(String message, LocalDate dueDate) {
        this.message = message;
        this.dueDate = dueDate;
    }

    public String getMessage() {
        return message;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    @Override
    public String toString() {
        return message + "Due: " + dueDate.toString();
    }
}
