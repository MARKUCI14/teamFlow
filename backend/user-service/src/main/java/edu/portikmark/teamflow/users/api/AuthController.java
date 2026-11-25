package edu.portikmark.teamflow.users.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import edu.portikmark.teamflow.users.application.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthService.AccessToken register(@Valid @RequestBody Credentials request) {
        return auth.register(request.email(), request.password());
    }

    @PostMapping("/login")
    public AuthService.AccessToken login(@Valid @RequestBody Credentials request) {
        return auth.login(request.email(), request.password());
    }

    public record Credentials(@NotBlank @Email @Size(max = 320) String email,
                              @NotBlank @Size(min = 8, max = 72) String password) { }
}
