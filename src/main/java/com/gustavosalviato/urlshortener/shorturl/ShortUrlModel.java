package com.gustavosalviato.urlshortener.shorturl;

import com.gustavosalviato.urlshortener.user.UserModel;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity(name = "tb_short_urls")
public class ShortUrlModel {
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;
    private String originalUrl;
    private String shortCode;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserModel user;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
