package com.markstart.urlshortener.repository;

import com.markstart.urlshortener.model.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {

    boolean existsByAlias(String alias);
    Optional<UrlMapping> findByAlias(String alias);
    void deleteByAlias(String alias);

}
