package com.gustavosalviato.urlshortener.user;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private IUserRepository userRepository;

    @PostMapping("/")
    public ResponseEntity Create(@RequestBody UserModel request) {
        var user = this.userRepository.findByEmail(request.getEmail());

        if (user != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User already exists");
        }

        var passwordHashed = BCrypt.withDefaults().hashToString(12, request.getPassword().toCharArray());

        request.setPassword(passwordHashed);

        var response = this.userRepository.save(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
