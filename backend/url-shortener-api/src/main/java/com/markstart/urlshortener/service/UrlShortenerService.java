package com.markstart.urlshortener.service;

import com.markstart.urlshortener.dto.ShortenUrlRequest;
import com.markstart.urlshortener.dto.UrlSummaryResponse;
import com.markstart.urlshortener.exception.AliasAlreadyExistsException;
import com.markstart.urlshortener.exception.AliasNotFoundException;
import com.markstart.urlshortener.model.UrlMapping;
import com.markstart.urlshortener.repository.UrlMappingRepository;
import com.markstart.urlshortener.util.UrlBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;

import static com.markstart.urlshortener.util.Constants.MAX_CUSTOM_ALIAS_LENGTH;


@Service
@RequiredArgsConstructor
public class UrlShortenerService {

    private static final String VALID_ALIAS_END_CHARACTERS = "abcdefghijklmnopqrstuvwxyz";
    private static final String VALID_ALIAS_MIDDLE_CHARACTERS = "-abcdefghijklmnopqrstuvwxyz";
    private static final int ALIAS_LENGTH = MAX_CUSTOM_ALIAS_LENGTH;
    private static final int MAX_GENERATION_ATTEMPTS = 8;

    private final UrlMappingRepository urlMappingRepository;
    private final UrlBuilder urlBuilder;
    private final SecureRandom secureRandom = new SecureRandom();

    public String createAndSaveUrlMapping(ShortenUrlRequest request) {

        String alias;
        String customAlias = request.getCustomAlias();

        if (customAlias == null) {
            alias = generateUniqueAlias();
        } else {
            checkCustomAliasAvailable(customAlias);
            alias = customAlias;
        }

        UrlMapping urlMapping = UrlMapping.builder()
                .alias(alias)
                .fullUrl(request.getFullUrl())
                .build();

        urlMappingRepository.save(urlMapping);

        String shortUrl = urlBuilder.buildShortUrl(alias);

        return shortUrl;

    }

    private String generateUniqueAlias() {

        int generationAttempt = 0;

        while (generationAttempt < MAX_GENERATION_ATTEMPTS) {

            StringBuilder stringBuilder = new StringBuilder(ALIAS_LENGTH);

            for (int i = 0; i < ALIAS_LENGTH; i++) {

                if (i == 0 || i == ALIAS_LENGTH - 1) {
                    stringBuilder.append(VALID_ALIAS_END_CHARACTERS.charAt(secureRandom.nextInt(VALID_ALIAS_END_CHARACTERS.length())));
                } else {
                    stringBuilder.append(VALID_ALIAS_MIDDLE_CHARACTERS.charAt(secureRandom.nextInt(VALID_ALIAS_MIDDLE_CHARACTERS.length())));
                }

            }

            String newlyGeneratedAlias = stringBuilder.toString();

            if (!urlMappingRepository.existsByAlias(newlyGeneratedAlias)) {
                return newlyGeneratedAlias;
            }

            generationAttempt++;

        }

        throw new IllegalStateException("max alias generation attempts exceeded");

    }

    private void checkCustomAliasAvailable(String customAlias) {

        if (urlMappingRepository.existsByAlias(customAlias)) {
            throw new AliasAlreadyExistsException(customAlias);
        }

    }



    public UrlMapping getUrlMappingByAlias(String alias) {

        return urlMappingRepository.findByAlias(alias)
                .orElseThrow(() -> new AliasNotFoundException(alias));

    }


    @Transactional
    public void deleteUrlMappingByAlias(String alias) {

        if (!urlMappingRepository.existsByAlias(alias)) {
            throw new AliasNotFoundException(alias);
        }

        urlMappingRepository.deleteByAlias(alias);

    }


    public List<UrlSummaryResponse> getAllUrlSummaries() {

        return urlMappingRepository.findAll()
                .stream()
                .map(this::toUrlSummaryResponse)
                .toList();

    }

    private UrlSummaryResponse toUrlSummaryResponse(UrlMapping urlMapping) {

        return new UrlSummaryResponse(
                urlMapping.getAlias(),
                urlMapping.getFullUrl(),
                urlBuilder.buildShortUrl(urlMapping.getAlias())
        );

    }


}
