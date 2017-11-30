package com.jodev.easyexport.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class EasyExportUtil {
    private static Logger LOGGER = LoggerFactory.getLogger(EasyExportUtil.class);

    public static String getQueryString(Map<String, String> param) {
        List<String> paramPairs = param.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.toList());
        return "/?" + paramPairs.stream().collect(Collectors.joining("&"));
    }

    public static String encodeURIComponent(String s)
    {
        try {
            return URLEncoder.encode(s, "UTF-8")
                    .replaceAll("\\+", "%20")
                    .replaceAll("\\%21", "!")
                    .replaceAll("\\%27", "'")
                    .replaceAll("\\%28", "(")
                    .replaceAll("\\%29", ")")
                    .replaceAll("\\%7E", "~");
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Could not encode string " + s + ". Error: " + e.getMessage());
            return s;
        }
    }
}
