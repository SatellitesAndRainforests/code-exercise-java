package com.markstart.urlshortener.service;

import com.markstart.urlshortener.dto.ShortenUrlRequest;
import com.markstart.urlshortener.exception.AliasAlreadyExistsException;
import com.markstart.urlshortener.model.UrlMapping;
import com.markstart.urlshortener.repository.UrlMappingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.markstart.urlshortener.util.Constants.CUSTOM_ALIAS_REGEX;
import static com.markstart.urlshortener.util.Constants.MAX_CUSTOM_ALIAS_LENGTH;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlShortenerServiceTest {

    @Mock
    private UrlMappingRepository urlMappingRepository;

    @InjectMocks
    private UrlShortenerService urlShortenerService;


    @Test
    void whenCustomAliasIsAvailable_savesUrlMappingAndReturnsCustomAlias() {

        // Arrange

        ShortenUrlRequest request = new ShortenUrlRequest();
        request.setFullUrl("https://example.com");
        String customAlias = "custom-alias";
        request.setCustomAlias(customAlias);

        when(urlMappingRepository.existsByAlias(customAlias)).thenReturn(false);


        // Act
        String alias = urlShortenerService.createAndSaveUrlMapping(request);

        // Assert

        assertEquals(customAlias, alias);
        verify(urlMappingRepository).save(any());

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

    }

    @Test
    void whenCustomAliasIsNull_generatesNewAliasSavesUrlMapping_returnsNewAlias() {

        // Arrange
        ShortenUrlRequest request = new ShortenUrlRequest();
        request.setFullUrl("https://example.com");
        request.setCustomAlias(null);

        when(urlMappingRepository.existsByAlias(anyString())).thenReturn(false);

        // Act
        String createdAlias = urlShortenerService.createAndSaveUrlMapping(request);

        // Assert
        assertNotNull(createdAlias);
        assertEquals(MAX_CUSTOM_ALIAS_LENGTH, createdAlias.length());
        assertTrue(createdAlias.matches(CUSTOM_ALIAS_REGEX));

        ArgumentCaptor<UrlMapping> captor = ArgumentCaptor.forClass(UrlMapping.class);
        verify(urlMappingRepository).save(captor.capture());

        UrlMapping savedMapping = captor.getValue();
        assertEquals(createdAlias, savedMapping.getAlias());
        assertEquals(request.getFullUrl(), savedMapping.getFullUrl());

    }

    @Test
    void whenGeneratedAliasExistsInTheRepository_serviceRetriesUntilAvailableAliasIsGenerated() {

        // Arrange
        ShortenUrlRequest request = new ShortenUrlRequest();
        request.setFullUrl("https://example.com");
        request.setCustomAlias(null);

        when(urlMappingRepository.existsByAlias(anyString()))
                .thenReturn(true, true, false);

        // Act
        String createdAlias = urlShortenerService.createAndSaveUrlMapping(request);

        // Assert
        assertNotNull(createdAlias);
        assertEquals(MAX_CUSTOM_ALIAS_LENGTH, createdAlias.length());
        assertTrue(createdAlias.matches(CUSTOM_ALIAS_REGEX));

        ArgumentCaptor<UrlMapping> captor = ArgumentCaptor.forClass(UrlMapping.class);
        verify(urlMappingRepository).save(captor.capture());
        verify(urlMappingRepository, times(3)).existsByAlias(anyString());

        UrlMapping savedMapping = captor.getValue();
        assertEquals(createdAlias, savedMapping.getAlias());
        assertEquals(request.getFullUrl(), savedMapping.getFullUrl());

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
    }

}
