package com.markstart.urlshortener.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public final class UrlBuilder {

    private final String baseUrl;

    public UrlBuilder(@Value("${app.base-url}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    // todo: test

    public String buildShortUrl(String alias) {
        return baseUrl + "/" + alias;
    }

}