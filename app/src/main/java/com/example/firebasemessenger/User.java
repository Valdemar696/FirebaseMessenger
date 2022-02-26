package com.example.firebasemessenger;

public class User {

    private String name;
    private String email;
    private String firebaseId;
    private int avatarMockUpResource;

    public User() {
    }

    public User(String name, String email, String firebaseId, int avatarMockUpRecource) {
        this.name = name;
        this.email = email;
        this.firebaseId = firebaseId;
        this.avatarMockUpResource = avatarMockUpResource;
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

    public int getAvatarMockUpResource() {
        return avatarMockUpResource;
    }

    public void setAvatarMockUpResource(int avatarMockUpRecource) {
        this.avatarMockUpResource = avatarMockUpRecource;
    }
}
