package com.gustavosalviato.urlshortener.user;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.gustavosalviato.urlshortener.exceptions.InvalidCredentialsException;
import com.gustavosalviato.urlshortener.exceptions.UserAlreadyExistsException;
import com.gustavosalviato.urlshortener.user.communication.CreateUserRequest;
import com.gustavosalviato.urlshortener.user.communication.CreateUserResponse;
import com.gustavosalviato.urlshortener.user.communication.LoginRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private IUserRepository userRepository;

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

        return ResponseEntity.ok("Authenticated");
    }
}
