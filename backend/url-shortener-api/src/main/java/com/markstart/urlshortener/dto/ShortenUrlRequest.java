package com.markstart.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.markstart.urlshortener.util.Constants.ALIAS_REGEX;
import static com.markstart.urlshortener.util.Constants.MAX_ALIAS_LENGTH;


@Getter
@Setter
@NoArgsConstructor
public class ShortenUrlRequest {

        @NotBlank(message = "fullUrl is required")
        String fullUrl;

        @Size(
                max = MAX_ALIAS_LENGTH,
                message = "customAlias must be at most {max} characters")
        @Pattern(
                regexp = ALIAS_REGEX,
                message = "if present, customAlias must start and end with a lower case letter and only contain lowercase letters and hyphens, no consecutive hyphens"
        )
        String customAlias;

}
