package edu.portikmark.teamflow.users.api;

import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @GetMapping("/api/users/me")
    public CurrentUser currentUser(@AuthenticationPrincipal Jwt token) {
        List<String> roles = token.getClaimAsStringList("roles");
        return new CurrentUser(UUID.fromString(token.getSubject()), token.getClaimAsString("email"), roles);
    }

    public record CurrentUser(UUID id, String email, List<String> roles) { }
}
