package com.gustavosalviato.urlshortener.user.communication;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateUserResponse(
        UUID id,
        String name,
        String email,
        LocalDateTime createdAt
) {
}
