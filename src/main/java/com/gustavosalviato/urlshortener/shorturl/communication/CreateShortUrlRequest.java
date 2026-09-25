package com.gustavosalviato.urlshortener.shorturl.communication;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record CreateShortUrlRequest(
        @NotBlank(message = "URL is required")
        @URL(message = "URL must be valid")
        String originalUrl) { }