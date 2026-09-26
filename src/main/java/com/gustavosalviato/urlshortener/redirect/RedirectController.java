package com.gustavosalviato.urlshortener.redirect;

import com.gustavosalviato.urlshortener.exceptions.ShortUrlNotFoundException;
import com.gustavosalviato.urlshortener.shorturl.IShortUrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/r")
public class RedirectController {
    @Autowired
    private IShortUrlRepository shortUrlRepository;


    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        var shortUrl = this.shortUrlRepository.findByShortCode(shortCode).orElseThrow(ShortUrlNotFoundException::new);

        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(shortUrl.getOriginalUrl())).build();
    }
}
