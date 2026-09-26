package com.gustavosalviato.urlshortener.shorturl;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IShortUrlRepository extends JpaRepository<ShortUrlModel, UUID> {
    boolean existsByShortCode(String shortCode);
    Optional<ShortUrlModel> findByShortCode(String shortCode);
    List<ShortUrlModel> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
}

