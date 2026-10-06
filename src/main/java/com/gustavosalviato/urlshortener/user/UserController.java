package com.gustavosalviato.urlshortener.user;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.gustavosalviato.urlshortener.exceptions.InvalidCredentialsException;
import com.gustavosalviato.urlshortener.exceptions.UserAlreadyExistsException;
import com.gustavosalviato.urlshortener.security.JwtService;
import com.gustavosalviato.urlshortener.user.communication.*;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private IUserRepository userRepository;
    @Autowired
    JwtService jwtService;

    @PostMapping("/")
    public ResponseEntity<Object> create(@Valid @RequestBody CreateUserRequest request) {
        var user = this.userRepository.findByEmail(request.email());

        if (user != null) {
            throw new UserAlreadyExistsException();
        }

        var passwordHashed = BCrypt.withDefaults().hashToString(12, request.password().toCharArray());

        var newUser = new UserModel();

        newUser.setName(request.name());
        newUser.setEmail(request.email());
        newUser.setPassword(passwordHashed);

        var response = this.userRepository.save(newUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(new CreateUserResponse(response.getId(), response.getName(), response.getEmail(), response.getCreatedAt()));
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@Valid @RequestBody LoginRequest request) {
        var user = this.userRepository.findByEmail(request.email());

        if (user == null) {
            throw new InvalidCredentialsException();
        }

        var passwordMatches = BCrypt.verifyer().verify(request.password().toCharArray(), user.getPassword());


        if (!passwordMatches.verified) {
            throw new InvalidCredentialsException();
        }

        var accessToken = this.jwtService.generateToken(user);
        var refreshToken = this.jwtService.generateRefreshToken(user);

        return ResponseEntity.ok(new LoginResponse(accessToken, refreshToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Object> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            var decodedToken = jwtService.validateAccessToken(request.refreshToken());

            var userId = UUID.fromString(decodedToken.getSubject());

            var user = this.userRepository.findById(userId).orElseThrow();

            var accessToken = this.jwtService.generateToken(user);
            var refreshToken = this.jwtService.generateRefreshToken(user);

            return ResponseEntity.ok(new LoginResponse(accessToken, refreshToken));


        } catch (JWTVerificationException e) {
             System.out.println(e);
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid refresh token");
        }
    }
}
