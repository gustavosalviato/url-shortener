package com.gustavosalviato.urlshortener.shorturl.communication;

import com.gustavosalviato.urlshortener.shorturl.ShortUrlModel;

import java.time.LocalDateTime;
import java.util.UUID;

public record ShortUrlResponse(
        UUID id,
        String originalUrl,
        String shortCode,
        LocalDateTime createdAt
) {
    public static ShortUrlResponse from(ShortUrlModel shortUrlModel) {
        return new ShortUrlResponse(
                shortUrlModel.getId(),
                shortUrlModel.getOriginalUrl(),
                shortUrlModel.getShortCode(),
                LocalDateTime.now()
        );
    }

}