package com.gustavosalviato.urlshortener.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.gustavosalviato.urlshortener.user.UserModel;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class JwtService {
    private final Algorithm algorithm = Algorithm.HMAC256("my-super-secret-key");

    public String generateToken(UserModel user) {
        return JWT.create()
                .withIssuer("url-shortener")
                .withSubject(user.getId().toString())
                .withClaim("email", user.getEmail())
                .withClaim("type", "access")
                .withIssuedAt(Instant.now())
                .withExpiresAt(
                        Instant.now().plus(180, ChronoUnit.MINUTES)
                )
                .sign(algorithm);
    }


    public DecodedJWT validateToken(String token) {
        return JWT.require(algorithm)
                .withIssuer("url-shortener")
                .withClaim("type", "access")
                .build()
                .verify(token);
    }
}

