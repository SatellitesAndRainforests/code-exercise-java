package com.markstart.urlshortener.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class ShortenUrlResponse {

    private final String shortUrl;

}