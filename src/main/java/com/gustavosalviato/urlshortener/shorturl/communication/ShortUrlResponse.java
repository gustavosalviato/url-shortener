package com.gustavosalviato.urlshortener.shorturl.communication;

import java.time.LocalDateTime;
import java.util.UUID;

public record ShortUrlResponse(
        UUID id,
        String originalUrl,
        String shortCode,
        LocalDateTime createdAt
) {}