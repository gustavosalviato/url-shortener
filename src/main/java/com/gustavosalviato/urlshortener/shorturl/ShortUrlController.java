package com.gustavosalviato.urlshortener.shorturl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/urls")
public class ShortUrlController {

    @Autowired
    private IShortUrlRepository shortUrlRepository;

    @PostMapping("/")
    public ResponseEntity<Object> create(@RequestBody ShortUrlModel request) {
        var response = this.shortUrlRepository.save(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
