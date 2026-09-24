package com.gustavosalviato.urlshortener.shorturl;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IShortUrlRepository extends JpaRepository<ShortUrlModel, UUID> {
}
