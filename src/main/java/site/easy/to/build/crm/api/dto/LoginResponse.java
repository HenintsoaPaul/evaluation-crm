package site.easy.to.build.crm.api.dto;

import lombok.Data;

@Data
public class LoginResponse {

    private final String username;
    private boolean authenticated;
    private String error;

    private int id;
    private String email;
    private String role;

    public LoginResponse(String username) {
        this.username = username;
        this.authenticated = true;
        this.error = null;
    }
}
