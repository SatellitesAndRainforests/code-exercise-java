package com.markstart.urlshortener.controller.validator;

import com.markstart.urlshortener.dto.ShortenUrlRequest;
import org.springframework.stereotype.Component;

import java.net.URI;

import static com.markstart.urlshortener.util.Constants.ALIAS_REGEX;
import static com.markstart.urlshortener.util.Constants.MAX_ALIAS_LENGTH;


@Component
public class UrlShortenerRequestValidator {

    public ShortenUrlRequest normalizeAndValidateRequest(ShortenUrlRequest request ) {

        ShortenUrlRequest normalizedRequest = new ShortenUrlRequest();
        String normalizedFullUrl = request.getFullUrl().trim();

        normalizedRequest.setFullUrl(normalizedFullUrl);
        normalizedRequest.setCustomAlias(request.getCustomAlias());

        validateFullUrl(normalizedRequest.getFullUrl());

        return normalizedRequest;

    }

    private void validateFullUrl(String fullUrl) {

        URI uri;

        try {
            uri = URI.create(fullUrl);
        } catch (Exception e) {
            throw new IllegalArgumentException("fullUrl must be a valid absolute URL");
        }

        if (uri.getScheme() == null ||
                (!uri.getScheme().equalsIgnoreCase("http")
                        && !uri.getScheme().equalsIgnoreCase("https"))) {
            throw new IllegalArgumentException("fullUrl must start with http:// or https://");
        }

        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new IllegalArgumentException("fullUrl must be a valid absolute URL");
        }

    }

    public void validateAlias(String alias) {

        if (alias == null
                || alias.length() > MAX_ALIAS_LENGTH
                || !alias.matches(ALIAS_REGEX)) {
            throw new IllegalArgumentException("alias format is invalid");
        }

    }


}
