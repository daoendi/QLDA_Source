package org.example.htmlfx.dashboard;

import org.example.htmlfx.toolkits.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Notification_Service {

    private static final int DUE_DAYS = 14;

    private List<Notification> notifications = new ArrayList<>();
    private List<Notification> overdueNotifications = new ArrayList<>();

    public void start() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkDueBooks();
                checkOverdueBooks();
            }
        }, 0, 24 * 60 * 60 * 1000); // Chạy mỗi ngày
    }

    private void checkDueBooks() {
        String sql = "SELECT b.id, b.member_id, b.book_id, b.borrow_date, m.email, m.phone " +
                "FROM borrow b JOIN members m ON b.member_id = m.member_id " +
                "WHERE b.returned_date IS NULL AND b.borrow_date = ?";

        LocalDate dueDate = LocalDate.now().minusDays(DUE_DAYS - 1);
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, java.sql.Date.valueOf(dueDate));
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String ID = resultSet.getString("id");
                String memberId = resultSet.getString("member_id");
                String bookId = resultSet.getString("book_id");
                String email = resultSet.getString("email");
                String phone = resultSet.getString("phone");
                LocalDate borrowDate = resultSet.getDate("borrow_date").toLocalDate();

                // Tạo thông báo và thêm vào danh sách
                Notification notification = new Notification(
                "NOTICE: return due date is approaching - 1 day \n" +
                        "ID: " + ID + "\t" +
                        "Member: " + memberId + "\t" +
                        "Book: " + bookId + "\n" +
                        "Send notification to:\n" +
                        "Email: " + email + "\nPhone: " + phone + '\n', borrowDate.plusDays(DUE_DAYS)
                );
                notifications.add(notification);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void checkOverdueBooks() {
        String sql = "SELECT b.id, b.member_id, b.book_id, b.borrow_date, m.email, m.phone " +
                "FROM borrow b JOIN members m ON b.member_id = m.member_id " +
                "WHERE b.returned_date IS NULL AND b.borrow_date <= ?";

        LocalDate overdueDate = LocalDate.now().minusDays(DUE_DAYS);
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, java.sql.Date.valueOf(overdueDate));
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String ID = resultSet.getString("id");
                String memberId = resultSet.getString("member_id");
                String bookId = resultSet.getString("book_id");
                String email = resultSet.getString("email");
                String phone = resultSet.getString("phone");
                LocalDate borrowDate = resultSet.getDate("borrow_date").toLocalDate();

                // Tạo thông báo quá hạn và thêm vào danh sách
                Notification notification = new Notification(
                "WARNING: return due date is over \n" +
                        "ID: " + ID + "\t" +
                        "Member: " + memberId + "\t" +
                        "Book: " + bookId + "\n" +
                        "Send notification to:\n" +
                        "Email: " + email + "\nPhone: " + phone + '\n', borrowDate.plusDays(DUE_DAYS)
                );
                overdueNotifications.add(notification);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Tự động đẩy ra khỏi danh sách nếu sách đã được trả
        String checkReturnSql = "SELECT id FROM borrow WHERE returned_date IS NOT NULL";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(checkReturnSql)) {
            ResultSet resultSet = statement.executeQuery();

            List<String> returnedBookIds = new ArrayList<>();
            while (resultSet.next()) {
                returnedBookIds.add(resultSet.getString("id"));
            }

            notifications.removeIf(notification -> returnedBookIds.contains(notification.getMessage().split("\t")[0].split(": ")[1]));
            overdueNotifications.removeIf(notification -> returnedBookIds.contains(notification.getMessage().split("\t")[0].split(": ")[1]));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Notification> getNotifications() {
        List<Notification> allNotifications = new ArrayList<>(notifications);
        allNotifications.addAll(overdueNotifications);
        return allNotifications;
    }
}
