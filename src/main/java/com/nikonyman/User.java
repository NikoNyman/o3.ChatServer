package com.nikonyman;

// User luokka joka auttaa säilömään käyttäjätietoja //

public class User {
    String username;
    String password;
    String email;

    public User(String uname, String pword, String mail) {
        this.username = uname;
        this.password = pword;
        this.email = mail;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

}
