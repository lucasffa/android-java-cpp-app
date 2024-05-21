package me.ensine.app.entities;

public class User {
    private String token;
    private String name;
    private String lastLogin;
    private String uuid;
    private String role;

    public User(String token, String name, String lastLogin, String uuid, String role) {
        this.token = token;
        this.name = name;
        this.lastLogin = lastLogin;
        this.uuid = uuid;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public String getName() {
        return name;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public String getUuid() {
        return uuid;
    }

    public String getRole() {
        return role;
    }
}
