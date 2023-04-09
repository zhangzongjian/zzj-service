package com.zzj.util;

public class StringUtil {

    public static boolean containsAllIgnoreCase(String str, String... substrings) {
        for (String substring : substrings) {
            if (!str.toLowerCase().contains(substring.toLowerCase())) {
                return false;
            }
        }
        return true;
    }
}
