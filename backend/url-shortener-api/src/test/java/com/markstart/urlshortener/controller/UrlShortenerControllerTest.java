package com.markstart.urlshortener.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.markstart.urlshortener.dto.ShortenUrlRequest;
import com.markstart.urlshortener.dto.UrlSummaryResponse;
import com.markstart.urlshortener.exception.AliasNotFoundException;
import com.markstart.urlshortener.model.UrlMapping;
import com.markstart.urlshortener.service.UrlShortenerService;
import com.markstart.urlshortener.controller.validator.UrlShortenerRequestValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.doThrow;


@WebMvcTest(UrlShortenerController.class)
@Import(UrlShortenerRequestValidator.class)
class UrlShortenerControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private UrlShortenerService urlShortenerService;

    // - - post

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

        String testShortUrl = "http://localhost:8080/test-alias";

        when(urlShortenerService.createAndSaveUrlMapping(any(ShortenUrlRequest.class)))
                .thenReturn(testShortUrl);

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

    // -- get

    @Test
    void getByAlias_whenAliasExists_returns302RedirectToFullUrl() throws Exception {

        String testAlias = "test-alias";
        String fullUrl = "https://example.com/very/long/url";

        UrlMapping urlMapping = UrlMapping.builder()
                .alias(testAlias)
                .fullUrl(fullUrl)
                .build();

        when(urlShortenerService.getUrlMappingByAlias(testAlias))
                .thenReturn(urlMapping);

        mockMvc.perform(get("/" + testAlias))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", fullUrl));
    }

    @Test
    void getByAlias_whenAliasDoesNotExist_returns404NotFound() throws Exception {

        String missingAlias = "missing-alias";

        when(urlShortenerService.getUrlMappingByAlias(missingAlias))
                .thenThrow(new AliasNotFoundException(missingAlias));

        mockMvc.perform(get("/" + missingAlias ))
                .andExpect(status().isNotFound());
    }

    // -- delete

    @Test
    void deleteByAlias_whenAliasExists_returns204NoContent() throws Exception {

        String alias = "test-alias";

        doNothing().when(urlShortenerService).deleteUrlMappingByAlias(alias);

        mockMvc.perform(delete("/" + alias))
                .andExpect(status().isNoContent());

    }

    @Test
    void deleteByAlias_whenAliasDoesNotExist_returns404NotFound() throws Exception {

        String missingAlias = "missing-alias";

        doThrow(new AliasNotFoundException(missingAlias))
                .when(urlShortenerService)
                .deleteUrlMappingByAlias(missingAlias);

        mockMvc.perform(delete("/" + missingAlias))
                .andExpect(status().isNotFound());
    }

    // - - getAllUrls

    @Test
    void getAllUrls_returns200OkAndListOfUrlSummaries() throws Exception {

        List<UrlSummaryResponse> responses = List.of(
                new UrlSummaryResponse(
                        "first-alias",
                        "https://example.com/first/long/url",
                        "http://localhost:8080/first-alias"
                ),
                new UrlSummaryResponse(
                        "second-alias",
                        "https://example.com/second/long/url",
                        "http://localhost:8080/second-alias"
                )
        );

        when(urlShortenerService.getAllUrlSummaries()).thenReturn(responses);

        mockMvc.perform(get("/urls"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].alias").value("first-alias"))
                .andExpect(jsonPath("$[0].fullUrl").value("https://example.com/first/long/url"))
                .andExpect(jsonPath("$[0].shortUrl").value("http://localhost:8080/first-alias"))
                .andExpect(jsonPath("$[1].alias").value("second-alias"))
                .andExpect(jsonPath("$[1].fullUrl").value("https://example.com/second/long/url"))
                .andExpect(jsonPath("$[1].shortUrl").value("http://localhost:8080/second-alias"));
    }

}
