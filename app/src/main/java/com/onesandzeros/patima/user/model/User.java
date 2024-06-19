package com.onesandzeros.patima.user.model;

public class User {
    private int user_id;
    private String fname;
    private String lname;
    private String email;
    private String profile_picture;
    private String password;
    private int role;

    private int archeologist_id;

    public User(String fname, String lname, String email, String password, int role) {
        this.fname = fname;
        this.lname = lname;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public User(String fname, String lname, String email, String password, int role, int archeologist_id) {
        this.fname = fname;
        this.lname = lname;
        this.email = email;
        this.password = password;
        this.role = role;
        this.archeologist_id = archeologist_id;
    }

    public User(int user_id, String fname, String lname, String email, int role, int archeologist_id) {
        this.user_id = user_id;
        this.fname = fname;
        this.lname = lname;
        this.email = email;
        this.role = role;
        this.archeologist_id = archeologist_id;
    }

    public int getUserId() {
        return user_id;
    }

    public String getFname() {
        return fname;
    }

    public String getLname() {
        return lname;
    }

    public String getEmail() {
        return email;
    }

    public String getProfilePicture() {
        return profile_picture;
    }

    public int getRole() {
        return role;
    }

    public int getArcheologistId() {
        return archeologist_id;
    }
}

