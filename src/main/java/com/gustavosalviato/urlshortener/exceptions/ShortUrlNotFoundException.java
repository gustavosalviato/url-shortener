package com.gustavosalviato.urlshortener.exceptions;

public class ShortUrlNotFoundException extends RuntimeException {
    public ShortUrlNotFoundException() {
        super("Short URL not found");
    }
}
