package com.markstart.urlshortener.util;


public final class Constants {

    public static final int MAX_ALIAS_LENGTH = 16;

    //    starts with letter
    //    ends with letter
    //    lowercase letters and hyphens only
    //    no double hyphens
    public static final String ALIAS_REGEX =  "^[a-z]+(?:-[a-z]+)*$";

    private Constants() {
    }

}
