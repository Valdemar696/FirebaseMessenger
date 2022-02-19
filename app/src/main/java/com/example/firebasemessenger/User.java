package com.example.firebasemessenger;

public class User {

    private String name;
    private String email;
    private String firebaseId;

    public User() {
    }

    public User(String name, String email, String firebaseId) {
        this.name = name;
        this.email = email;
        this.firebaseId = firebaseId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirebaseId() {
        return firebaseId;
    }

    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }
}
