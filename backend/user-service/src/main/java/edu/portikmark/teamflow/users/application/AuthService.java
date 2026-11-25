package edu.portikmark.teamflow.users.application;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import edu.portikmark.teamflow.users.domain.User;
import edu.portikmark.teamflow.users.infrastructure.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwords;
    private final JwtEncoder tokens;
    private final String issuer;
    private final long ttlSeconds;

    public AuthService(UserRepository users, PasswordEncoder passwords, JwtEncoder tokens,
                       @Value("${teamflow.security.issuer}") String issuer,
                       @Value("${teamflow.security.access-token-ttl}") java.time.Duration ttl) {
        this.users = users;
        this.passwords = passwords;
        this.tokens = tokens;
        this.issuer = issuer;
        this.ttlSeconds = ttl.toSeconds();
    }

    @Transactional
    public AccessToken register(String email, String password) {
        String normalizedEmail = email.trim().toLowerCase(java.util.Locale.ROOT);
        if (users.existsByEmail(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        }
        return issue(users.save(new User(normalizedEmail, passwords.encode(password))));
    }

    @Transactional(readOnly = true)
    public AccessToken login(String email, String password) {
        User user = users.findByEmail(email.trim().toLowerCase(java.util.Locale.ROOT))
                .filter(found -> passwords.matches(password, found.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        return issue(user);
    }

    private AccessToken issue(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer(issuer).subject(user.getId().toString())
                .issuedAt(now).expiresAt(now.plus(ttlSeconds, ChronoUnit.SECONDS))
                .claim("email", user.getEmail())
                .claim("roles", user.getGlobalRole() == null
                        ? java.util.List.of() : java.util.List.of(user.getGlobalRole()))
                .build();
        String token = tokens.encode(JwtEncoderParameters.from(
                org.springframework.security.oauth2.jwt.JwsHeader.with(SignatureAlgorithm.RS256).build(), claims))
                .getTokenValue();
        return new AccessToken(token, "Bearer", ttlSeconds);
    }

    public record AccessToken(String accessToken, String tokenType, long expiresIn) { }
}
