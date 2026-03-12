package com.markstart.urlshortener.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.markstart.urlshortener.dto.ShortenUrlRequest;
import com.markstart.urlshortener.service.UrlShortenerService;
import com.markstart.urlshortener.controller.validator.UrlShortenerRequestValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(UrlShortenerController.class)
@Import(UrlShortenerRequestValidator.class)
class UrlShortenerControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private UrlShortenerService urlShortenerService;


    static Stream<ShortenUrlRequest> validRequests() {

        ShortenUrlRequest request1 = new ShortenUrlRequest();
        request1.setFullUrl( "https://example.com" );
        request1.setCustomAlias("custom-alias");

        ShortenUrlRequest request2 = new ShortenUrlRequest();
        request2.setFullUrl( "http://website.com" );
        request2.setCustomAlias("website");

        ShortenUrlRequest request3 = new ShortenUrlRequest();
        request3.setFullUrl( "https://website.com" );

        ShortenUrlRequest request4 = new ShortenUrlRequest();
        request4.setFullUrl( "https://website.com" );
        request4.setCustomAlias(null);

        return Stream.of(
                request1,
                request2,
                request3,
                request4
        );

    }

    @ParameterizedTest
    @MethodSource("validRequests")
    void validShortenUrlRequests_return201Created(ShortenUrlRequest request) throws Exception {

        when(urlShortenerService.createAndSaveUrlMapping(any(ShortenUrlRequest.class)))
                .thenReturn(request.getCustomAlias());

        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }


    static Stream<ShortenUrlRequest> invalidRequests() {

        ShortenUrlRequest request1 = new ShortenUrlRequest();
        request1.setFullUrl( "example.com" );
        request1.setCustomAlias("custom-alias");

        ShortenUrlRequest request2 = new ShortenUrlRequest();
        request2.setFullUrl( "https://website.com" );
        request2.setCustomAlias("-custome-");

        ShortenUrlRequest request3 = new ShortenUrlRequest();
        request3.setFullUrl( "https://website.com" );
        request3.setCustomAlias("");

        return Stream.of(
                request1,
                request2,
                request3
        );

    }

    @ParameterizedTest
    @MethodSource("invalidRequests")
    void invalidShortenUrlRequests_return400BadRequest( ShortenUrlRequest invalidRequest) throws Exception {

        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

    }


}
