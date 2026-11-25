package edu.portikmark.teamflow.users.api;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.nimbusds.jose.jwk.RSAKey;

@RestController
public class JwksController {

    private final RSAKey key;

    public JwksController(RSAKey key) {
        this.key = key;
    }

    @GetMapping("/.well-known/jwks.json")
    public JwksResponse jwks() {
        RSAKey publicKey = key.toPublicJWK();
        Jwk jwk = new Jwk("RSA", "sig", "RS256", publicKey.getKeyID(),
                publicKey.getModulus().toString(), publicKey.getPublicExponent().toString());
        return new JwksResponse(List.of(jwk));
    }

    public record JwksResponse(List<Jwk> keys) {
    }

    public record Jwk(String kty, String use, String alg, String kid, String n, String e) {
    }
}
