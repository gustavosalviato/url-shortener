package com.gustavosalviato.urlshortener.shorturl;

import com.gustavosalviato.urlshortener.shorturl.communication.CreateShortUrlRequest;
import com.gustavosalviato.urlshortener.shorturl.communication.CreateShortUrlResponse;
import com.gustavosalviato.urlshortener.shorturl.communication.ShortUrlResponse;
import com.gustavosalviato.urlshortener.user.IUserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/urls")
public class ShortUrlController {

    @Autowired
    private IShortUrlRepository shortUrlRepository;

    @Autowired
    private IUserRepository userRepository;

    @PostMapping("/")
    public ResponseEntity<Object> create(@AuthenticationPrincipal UUID userId, @Valid @RequestBody CreateShortUrlRequest request) {
        var user = this.userRepository.findById(userId).orElseThrow();

        var shortUrl = new ShortUrlModel();

        shortUrl.setOriginalUrl(request.originalUrl());
        shortUrl.setShortCode(generateShortCode());
        shortUrl.setUser(user);


        var response = this.shortUrlRepository.save(shortUrl);


        return ResponseEntity.status(HttpStatus.CREATED).body(new CreateShortUrlResponse(response.getId(), response.getOriginalUrl(), response.getShortCode(), response.getCreatedAt()));
    }


    @GetMapping
    public ResponseEntity<List<ShortUrlResponse>> findAll(@AuthenticationPrincipal UUID userId) {

        var shortUrls = this.shortUrlRepository.findAllByUserIdOrderByCreatedAtDesc(userId);

        var response = shortUrls.stream()
                .map(shortUrl -> new ShortUrlResponse(
                        shortUrl.getId(), shortUrl.getOriginalUrl(), shortUrl.getShortCode(), shortUrl.getCreatedAt())
                ).toList();


        return ResponseEntity.ok(response);
    }


    private String generateShortCode() {
        String shortCode;

        do {
            shortCode = UUID.randomUUID().toString().replace("-", "").substring(0, 6);

        } while (shortUrlRepository.existsByShortCode(shortCode));

        return shortCode;
    }

}
