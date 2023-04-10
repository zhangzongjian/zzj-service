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

    /**
     * 将字符串中所有的“{}”替换为“%s”，然后使用String.format()将参数插入字符串中
     */
    public static String format(String str, Object... args) {
        return String.format(str.replace("{}", "%s"), args);
    }
}
