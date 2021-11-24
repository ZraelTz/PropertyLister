package com.ztech.propertylister.utils;

import java.util.regex.Pattern;

public class Regex {
    public static final Pattern PASSWORD_REGEX = Pattern.compile("^" +
            "(?=.*[@#$%^&+=])" +     // at least 1 special character
            "(?=\\S+$)" +            // no white spaces
            ".{8,}" +                // at least 8 characters
            "$");
}
