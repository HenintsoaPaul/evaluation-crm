package site.easy.to.build.crm.api.dto;

import lombok.Data;

@Data
public class LoginResponse {

    private final String username;
    private boolean authenticated;
    private String error;

    public LoginResponse(String username) {
        this.username = username;
        this.authenticated = true;
        this.error = null;
    }
}
