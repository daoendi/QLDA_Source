package org.example.htmlfx.toolkits;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public class Checked {
    public static boolean isValidDate(String date) {
        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
            LocalDate.parse(date, fmt);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@gmail.com";
        return email.matches(emailRegex);
    }

    public static boolean isValidPhone(String phone) {
        return phone.matches("0\\d{9}"); // Bắt đầu bằng chữ số '0' và theo sau là 9 chữ số.
    }

    public static boolean passwordsMatch(String password, String confirm) {
        if (password == null) return confirm == null;
        return password.equals(confirm);
    }

    public static boolean isStrongPassword(String password) {
        if (password == null) return false;
        if (password.length() < 8) return false;
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(c -> "@#$%^&+=!._-".indexOf(c) >= 0);
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

}
