package com.markstart.urlshortener.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UrlSummaryResponse {

    private final String alias;
    private final String fullUrl;
    private final String shortUrl;
}