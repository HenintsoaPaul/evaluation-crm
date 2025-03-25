package site.easy.to.build.crm.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import site.easy.to.build.crm.api.*;
import site.easy.to.build.crm.api.dto.LoginRequest;
import site.easy.to.build.crm.api.dto.LoginResponse;
import site.easy.to.build.crm.entity.*;
import site.easy.to.build.crm.repository.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/api/login")
@RequiredArgsConstructor
public class LoginApiController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final List<String> unauthorized = List.of("ROLE_CUSTOMER");

    @PostMapping
    public ResponseEntity<ApiResponse<?>> authenticate(
            @RequestBody LoginRequest loginRequest
    ) {
        LoginResponse response = new LoginResponse(loginRequest.getUsername());
        try {
            List<User> users = userRepository.findByUsername(loginRequest.getUsername());
            User user = users.isEmpty() ? null : users.get(0);

            if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                throw new ApiServerException("Username or password incorrect");
            }

            if (user.getStatus().equals("suspended")) {
                throw new ApiServerException("Account suspended");
            } else if (user.getStatus().equals("inactive")) {
                throw new ApiServerException("Account inactive");
            }

            if (unauthorized.contains(user.getUsername())) {
                throw new ApiServerException("Account not authorized");
            }

            response.setAuthenticated(true);
            response.setError(null);
            response.setId(user.getId());
            response.setEmail(user.getEmail());
            response.setRole(user.getRoles().get(0).getName());
            response.setToken(user.getToken());

            return ResponseEntity.ok(new ApiOkResponse<>("Connexion ok!", response));

        } catch (ApiServerException e) {
            response.setAuthenticated(false);
            response.setError(e.getMessage());

            return new ResponseEntity<>(new ApiBadResponse<>("Connexion echoue!", response), HttpStatus.BAD_REQUEST);
        }
    }

}
