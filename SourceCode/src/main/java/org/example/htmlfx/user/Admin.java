package org.example.htmlfx.user;

import java.util.Date;

public class Admin extends Users {
    private String password;
    private String admin_name;

    public Admin() {
        super();
    }

    public Admin(String id, String firstname, String lastname, String gender, String birthday, String email, String phone, String password, String admin_name, String image) {
        super(id, firstname, lastname, gender, birthday, email, phone, image);
        this.password = password;
        this.admin_name = admin_name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAdmin_name() {
        return admin_name;
    }

    public void setAdmin_name(String admin_name) {
        this.admin_name = admin_name;
    }
}
