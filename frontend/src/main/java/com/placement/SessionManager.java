package com.placement;

public class SessionManager {
    private static SessionManager instance;
    private String token;
    private String role;
    private String email;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public void setSession(String token, String role, String email) {
        this.token = token;
        this.role = role;
        this.email = email;
    }

    public String getToken() { return token; }
    public String getRole() { return role; }
    public String getEmail() { return email; }
    public void clearSession() { token = null; role = null; email = null; }
}