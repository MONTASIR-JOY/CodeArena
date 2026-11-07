package com.codearena.util;

import com.codearena.data.model.CfBlogEntry;
import com.codearena.data.model.CfRecentAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlogFeed {

    public enum Sort { NEWEST, ACTIVE }

    public static List<CfBlogEntry> blogs(List<CfRecentAction> actions, Sort sort) {
        if (actions == null) return Collections.emptyList();
        List<CfBlogEntry> distinct = new ArrayList<>();
        Set<Long> seen = new HashSet<>();

        for (CfRecentAction action : actions) {
            if (action.getBlogEntry() != null && !seen.contains(action.getBlogEntry().getId())) {
                seen.add(action.getBlogEntry().getId());
                distinct.add(action.getBlogEntry());
            }
        }

        if (sort == Sort.NEWEST) {
            Collections.sort(distinct, (a, b) -> Long.compare(b.getCreationTimeSeconds(), a.getCreationTimeSeconds()));
        }
        return distinct;
    }

    public static List<CfBlogEntry> byAuthors(List<CfBlogEntry> blogs, Set<String> handles) {
        if (blogs == null || handles == null) return Collections.emptyList();
        Set<String> lower = new HashSet<>();
        for (String h : handles) {
            if (h != null) lower.add(h.toLowerCase());
        }

        List<CfBlogEntry> filtered = new ArrayList<>();
        for (CfBlogEntry b : blogs) {
            if (b.getAuthorHandle() != null && lower.contains(b.getAuthorHandle().toLowerCase())) {
                filtered.add(b);
            }
        }
        return filtered;
    }

    public static String timeAgo(long nowSeconds, long thenSeconds) {
        long diff = Math.max(0, nowSeconds - thenSeconds);
        if (diff < 60) return "just now";
        if (diff < 3600) return (diff / 60) + " min ago";
        if (diff < 86400) return (diff / 3600) + " h ago";
        if (diff < 30 * 86400L) return (diff / 86400) + " d ago";
        return (diff / (30 * 86400L)) + " mo ago";
    }

    public static String prepareHtml(String html) {
        if (html == null) return "";
        return html.replaceAll("(?is)<(script|style)[^>]*>.*?</\\1>", "")
                   .replaceAll("(?i)<img[^>]*>", "[image]");
    }
}

