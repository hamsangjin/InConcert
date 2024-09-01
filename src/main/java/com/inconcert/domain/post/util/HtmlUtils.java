package com.inconcert.domain.post.util;

public class HtmlUtils {
    public static String escapeHtml(String input) {
        return input.replaceAll("&", "&amp;")
                    .replaceAll("'", "&#39;");
    }
}