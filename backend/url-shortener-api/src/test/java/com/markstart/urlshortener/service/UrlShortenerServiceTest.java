package com.markstart.urlshortener.service;

import com.markstart.urlshortener.dto.ShortenUrlRequest;
import com.markstart.urlshortener.dto.UrlSummaryResponse;
import com.markstart.urlshortener.exception.AliasAlreadyExistsException;
import com.markstart.urlshortener.exception.AliasNotFoundException;
import com.markstart.urlshortener.model.UrlMapping;
import com.markstart.urlshortener.repository.UrlMappingRepository;
import com.markstart.urlshortener.util.UrlBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.markstart.urlshortener.util.Constants.ALIAS_REGEX;
import static com.markstart.urlshortener.util.Constants.MAX_ALIAS_LENGTH;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlShortenerServiceTest {

    @Mock
    private UrlMappingRepository urlMappingRepository;

    @Mock
    private UrlBuilder urlBuilder;

    @InjectMocks
    private UrlShortenerService urlShortenerService;


    // - - post

    @Test
    void whenCustomAliasIsAvailable_savesUrlMappingAndReturnsShortUrl() {

        // Arrange

        ShortenUrlRequest request = new ShortenUrlRequest();
        request.setFullUrl("https://example.com");
        String customAlias = "custom-alias";
        request.setCustomAlias(customAlias);

        String expectedShortUrl = "http://localhost:8080/custom-alias";

        when(urlMappingRepository.existsByAlias(customAlias)).thenReturn(false);
        when(urlBuilder.buildShortUrl(customAlias)).thenReturn(expectedShortUrl);

        // Act
        String returnedShortUrl = urlShortenerService.createAndSaveUrlMapping(request);

        // Assert
        assertEquals(expectedShortUrl, returnedShortUrl);

        ArgumentCaptor<UrlMapping> captor = ArgumentCaptor.forClass(UrlMapping.class);
        verify(urlMappingRepository).save(captor.capture());

        UrlMapping savedMapping = captor.getValue();
        assertEquals(request.getCustomAlias(), savedMapping.getAlias());
        assertEquals(request.getFullUrl(), savedMapping.getFullUrl());

        verify(urlBuilder).buildShortUrl(customAlias);

    }

    @Test
    void whenCustomAliasAlreadyExists_throwsCustomAliasAlreadyExistsException() {

        // Arrange

        ShortenUrlRequest request = new ShortenUrlRequest();
        request.setFullUrl("https://example.com");
        String customAlias = "exists-in-repository";
        request.setCustomAlias(customAlias);

        when(urlMappingRepository.existsByAlias(customAlias)).thenReturn(true);

        // Act + Assert

        assertThrows(AliasAlreadyExistsException.class,
                () -> urlShortenerService.createAndSaveUrlMapping(request));

        verify(urlMappingRepository, never()).save(any());
        verify(urlBuilder, never()).buildShortUrl(any());

    }

    @Test
    void whenCustomAliasIsNull_generatesNewAliasSavesUrlMapping_returnsGeneratedShortUrl() {

        // Arrange
        ShortenUrlRequest request = new ShortenUrlRequest();
        request.setFullUrl("https://example.com");
        request.setCustomAlias(null);

        String testBaseUrl = "http://localhost:8080/";

        when(urlMappingRepository.existsByAlias(anyString())).thenReturn(false);

        when(urlBuilder.buildShortUrl(anyString()))
                .thenAnswer(invocation -> testBaseUrl + invocation.getArgument(0, String.class));

        // Act
        String createdShortUrl  = urlShortenerService.createAndSaveUrlMapping(request);

        // Assert
        ArgumentCaptor<UrlMapping> captor = ArgumentCaptor.forClass(UrlMapping.class);
        verify(urlMappingRepository).save(captor.capture());

        UrlMapping savedMapping = captor.getValue();
        String generatedAlias = savedMapping.getAlias();

        assertNotNull(createdShortUrl );
        assertEquals(MAX_ALIAS_LENGTH, generatedAlias.length());
        assertTrue(generatedAlias.matches(ALIAS_REGEX));

        String expectedShortUrl = testBaseUrl + generatedAlias;

        assertEquals(expectedShortUrl, createdShortUrl);
        assertEquals(request.getFullUrl(), savedMapping.getFullUrl());

    }

    @Test
    void whenGeneratedAliasExistsInTheRepository_serviceRetriesUntilAvailableAliasIsGenerated() {

        // Arrange
        ShortenUrlRequest request = new ShortenUrlRequest();
        request.setFullUrl("https://example.com/very/long/url");
        request.setCustomAlias(null);

        String testBaseUrl = "http://localhost:8080/";

        when(urlMappingRepository.existsByAlias(anyString()))
                .thenReturn(true, true, false);

        when(urlBuilder.buildShortUrl(anyString()))
                .thenAnswer(invocation -> testBaseUrl + invocation.getArgument(0, String.class));

        // Act
        String returnedShortUrl = urlShortenerService.createAndSaveUrlMapping(request);

        // Assert
        ArgumentCaptor<UrlMapping> captor = ArgumentCaptor.forClass(UrlMapping.class);
        verify(urlMappingRepository).save(captor.capture());
        verify(urlMappingRepository, times(3)).existsByAlias(anyString());

        UrlMapping savedMapping = captor.getValue();
        String generatedAlias = savedMapping.getAlias();

        assertNotNull(generatedAlias);
        assertEquals(MAX_ALIAS_LENGTH, generatedAlias.length());
        assertTrue(generatedAlias.matches(ALIAS_REGEX));
        assertEquals(request.getFullUrl(), savedMapping.getFullUrl());

        String expectedShortUrl = testBaseUrl + generatedAlias;

        assertEquals(expectedShortUrl, returnedShortUrl);
        verify(urlBuilder).buildShortUrl(generatedAlias);

    }

    @Test
    void whenGenerateAttemptsExceedsMaxAttempts_throwsIllegalStateException() {

        // Arrange
        ShortenUrlRequest request = new ShortenUrlRequest();
        request.setFullUrl("https://example.com");
        request.setCustomAlias(null);

        when(urlMappingRepository.existsByAlias(anyString())).thenReturn(true);

        // Act + Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> urlShortenerService.createAndSaveUrlMapping(request)
        );

        assertEquals("max alias generation attempts exceeded", exception.getMessage());
        verify(urlMappingRepository, times(8)).existsByAlias(anyString());
        verify(urlMappingRepository, never()).save(any());
        verify(urlBuilder, never()).buildShortUrl(any());
    }


    // - - get

    @Test
    void whenAliasExists_getUrlMappingByAlias_returnsUrlMapping() {

        // Arrange
        String alias = "test-alias";

        UrlMapping expectedUrlMapping = UrlMapping.builder()
                .alias(alias)
                .fullUrl("https://example.com/very/long/url")
                .build();

        when(urlMappingRepository.findByAlias(alias)).thenReturn(Optional.of(expectedUrlMapping));

        // Act
        UrlMapping returnedUrlMapping = urlShortenerService.getUrlMappingByAlias(alias);

        // Assert
        assertEquals(expectedUrlMapping, returnedUrlMapping);
        verify(urlMappingRepository).findByAlias(alias);

    }


    @Test
    void whenAliasDoesNotExist_getUrlMappingByAlias_throwsAliasNotFoundException() {

        // Arrange
        String alias = "missing-alias";

        when(urlMappingRepository.findByAlias(alias)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(AliasNotFoundException.class,
                () -> urlShortenerService.getUrlMappingByAlias(alias));

        verify(urlMappingRepository).findByAlias(alias);

    }


    // - - delete

    @Test
    void whenAliasExists_deleteUrlMappingByAlias_deletesUrlMapping() {

        // Arrange
        String alias = "test-alias";
        when(urlMappingRepository.existsByAlias(alias)).thenReturn(true);

        // Act
        urlShortenerService.deleteUrlMappingByAlias(alias);

        // Assert
        verify(urlMappingRepository).existsByAlias(alias);
        verify(urlMappingRepository).deleteByAlias(alias);

    }

    @Test
    void whenAliasDoesNotExist_deleteUrlMappingByAlias_throwsAliasNotFoundException() {

        // Arrange
        String alias = "missing-alias";
        when(urlMappingRepository.existsByAlias(alias)).thenReturn(false);

        // Act + Assert
        assertThrows(AliasNotFoundException.class,
                () -> urlShortenerService.deleteUrlMappingByAlias(alias));

        verify(urlMappingRepository).existsByAlias(alias);
        verify(urlMappingRepository, never()).deleteByAlias(anyString());

    }


    // - - getAllUrls

    @Test
    void getAllUrlSummaries_returnsMappedResponses() {

        UrlMapping firstMapping = UrlMapping.builder()
                .alias("first-alias")
                .fullUrl("https://example.com/first/long/url")
                .build();

        UrlMapping secondMapping = UrlMapping.builder()
                .alias("second-alias")
                .fullUrl("https://example.com/second/long/url")
                .build();

        when(urlMappingRepository.findAll()).thenReturn(List.of(firstMapping, secondMapping));
        when(urlBuilder.buildShortUrl("first-alias")).thenReturn("http://localhost:8080/first-alias");
        when(urlBuilder.buildShortUrl("second-alias")).thenReturn("http://localhost:8080/second-alias");

        List<UrlSummaryResponse> result = urlShortenerService.getAllUrlSummaries();

        assertEquals(2,result.size());

        assertThat(result.get(0).getAlias()).isEqualTo("first-alias");
        assertThat(result.get(0).getFullUrl()).isEqualTo("https://example.com/first/long/url");
        assertThat(result.get(0).getShortUrl()).isEqualTo("http://localhost:8080/first-alias");

        assertThat(result.get(1).getAlias()).isEqualTo("second-alias");
        assertThat(result.get(1).getFullUrl()).isEqualTo("https://example.com/second/long/url");
        assertThat(result.get(1).getShortUrl()).isEqualTo("http://localhost:8080/second-alias");
    }

}
