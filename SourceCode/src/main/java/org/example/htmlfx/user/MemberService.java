package org.example.htmlfx.user;

import org.example.htmlfx.toolkits.Checked;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MemberService {

    public boolean addMember(Connection connection,
                             String firstname,
                             String lastname,
                             String gender,
                             String birthday,
                             String phone,
                             String email) {
        if (firstname == null || firstname.isBlank() ||
            lastname == null || lastname.isBlank() ||
            gender == null || gender.isBlank() ||
            birthday == null || birthday.isBlank() ||
            phone == null || phone.isBlank() ||
            email == null || email.isBlank()) {
            return false;
        }
        if (!Checked.isValidDate(birthday)) return false;
        if (!Checked.isValidEmail(email)) return false;
        if (!Checked.isValidPhone(phone)) return false;

        String sql = "INSERT INTO members (firstname, lastname, gender, birth, phone, email) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, firstname);
            ps.setString(2, lastname);
            ps.setString(3, gender);
            ps.setString(4, birthday);
            ps.setString(5, phone);
            ps.setString(6, email);
            return ps.executeUpdate() > 0; // MEM-01
        } catch (SQLException e) {
            return false;
        }
    }

    public List<Member> searchMembers(Connection connection, String keyword) {
        List<Member> list = new ArrayList<>();
        if (keyword == null || keyword.isBlank()) return list;
        String sql = "SELECT * FROM members WHERE member_id = ? OR firstname LIKE ? OR lastname LIKE ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, keyword);
            String like = "%" + keyword + "%";
            ps.setString(2, like);
            ps.setString(3, like);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Member m = new Member(
                        rs.getString("member_id"),
                        rs.getString("firstname"),
                        rs.getString("lastname"),
                        rs.getString("gender"),
                        rs.getString("birth"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("image")
                );
                list.add(m);
            }
        } catch (SQLException e) {
            // ignore -> return current list
        }
        return list; // MEM-06
    }
}
