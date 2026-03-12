package com.markstart.urlshortener.exception;

public class AliasNotFoundException extends RuntimeException {

    public AliasNotFoundException(String alias) {
        super("Alias not found: " + alias);
    }

}
