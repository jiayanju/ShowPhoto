package com.jyj.cache;

public class CacheHelper {
    /**
     * Replace the special characters in Url to a single + symbol
     * 
     * @param url
     * @return
     */
    public static String getFileNameFromUrl(String url) {
        // replace all special URI characters with a single + symbol
        return url.replaceAll("[.:/,%?&=]", "+").replaceAll("[+]+", "+");
    }
}
