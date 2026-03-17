package com.markstart.urlshortener.controller;

import com.markstart.urlshortener.controller.validator.UrlShortenerRequestValidator;
import com.markstart.urlshortener.dto.ShortenUrlRequest;
import com.markstart.urlshortener.dto.ShortenUrlResponse;
import com.markstart.urlshortener.dto.UrlSummaryResponse;
import com.markstart.urlshortener.model.UrlMapping;
import com.markstart.urlshortener.service.UrlShortenerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;


@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequiredArgsConstructor
public class UrlShortenerController {

    private final UrlShortenerRequestValidator urlShortenerRequestValidator;
    private final UrlShortenerService urlShortenerService;

    @PostMapping("/shorten")
    public ResponseEntity<ShortenUrlResponse> shortenUrl(@Valid @RequestBody ShortenUrlRequest request) {

        ShortenUrlRequest normalizedRequest =
                urlShortenerRequestValidator.normalizeAndValidateRequest(request);

        String shortUrl = urlShortenerService.createAndSaveUrlMapping(normalizedRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ShortenUrlResponse(shortUrl));

    }

    @GetMapping("/{alias}")
    public ResponseEntity<Void> redirectToFullUrl(@PathVariable String alias) {

        urlShortenerRequestValidator.validateAlias(alias);

        UrlMapping urlMapping = urlShortenerService.getUrlMappingByAlias(alias);

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(urlMapping.getFullUrl()))
                .build();

    }

    @DeleteMapping("/{alias}")
    public ResponseEntity<Void> deleteByAlias(@PathVariable String alias) {

        urlShortenerRequestValidator.validateAlias(alias);

        urlShortenerService.deleteUrlMappingByAlias(alias);

        return ResponseEntity.noContent().build();

    }

    @GetMapping("/urls")
    public ResponseEntity<List<UrlSummaryResponse>> getAllUrls() {

        List<UrlSummaryResponse> responses = urlShortenerService.getAllUrlSummaries();

        return ResponseEntity.ok(responses);

    }

}
