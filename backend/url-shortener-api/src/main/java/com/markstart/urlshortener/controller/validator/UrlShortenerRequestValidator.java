package com.markstart.urlshortener.controller.validator;

import com.markstart.urlshortener.dto.ShortenUrlRequest;
import org.springframework.stereotype.Component;

import java.net.URI;


@Component
public class UrlShortenerRequestValidator {


    public void validateRequest(  ShortenUrlRequest request ) {

        validateFullUrl(request.getFullUrl());

    }

    private void validateFullUrl( String fullUrl ) {


        // starts with and ends with character no white space check

        try {

            URI uri = URI.create(fullUrl.trim());

            if (uri.getScheme() == null ||
                    (!uri.getScheme().equalsIgnoreCase("http") &&
                            !uri.getScheme().equalsIgnoreCase("https"))) {
                throw new IllegalArgumentException("fullUrl must start with http:// or https://");
            }

            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new IllegalArgumentException("fullUrl must be a valid absolute URL");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("fullUrl must be a valid absolute URL");
        }

    }



}
