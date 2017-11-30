package com.jodev.easyexport.model.page;

public class PageBasicAuthentication {
    private String username;
    private String password;

    public PageBasicAuthentication(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
