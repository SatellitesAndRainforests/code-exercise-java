package com.markstart.urlshortener.controller;

import com.markstart.urlshortener.controller.validator.UrlShortenerRequestValidator;
import com.markstart.urlshortener.dto.ShortenUrlRequest;
import com.markstart.urlshortener.dto.ShortenUrlResponse;
import com.markstart.urlshortener.service.UrlShortenerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UrlShortenerController {

    private final UrlShortenerRequestValidator urlShortenerRequestValidator;
    private final UrlShortenerService urlShortenerService;

    @PostMapping("/shorten")
    public ResponseEntity<ShortenUrlResponse> shortenUrl(@Valid @RequestBody ShortenUrlRequest request) {

        urlShortenerRequestValidator.validateRequest(request);

        String newlySavedAlias = urlShortenerService.createAndSaveUrlMapping(request);

        // todo: add new alias endpoint

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ShortenUrlResponse(newlySavedAlias));

    }

}
