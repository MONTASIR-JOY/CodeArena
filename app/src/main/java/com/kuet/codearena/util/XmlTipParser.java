package com.kuet.codearena.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.kuet.codearena.R;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility parser extracting competitive programming tactical tips from XML resources via XmlPullParser.
 */
public class XmlTipParser {
    private static final String TAG = "XmlTipParser";

    public static class TagTip {
        private final String tag;
        private final String title;
        private final String advice;

        public TagTip(String tag, String title, String advice) {
            this.tag = tag;
            this.title = title;
            this.advice = advice;
        }

        public String getTag() {
            return tag;
        }

        public String getTitle() {
            return title;
        }

        public String getAdvice() {
            return advice;
        }
    }

    public static Map<String, TagTip> parseTips(Context context) {
        Map<String, TagTip> map = new HashMap<>();
        try {
            Resources res = context.getResources();
            XmlPullParser parser = res.getXml(R.raw.tag_tips);

            int eventType = parser.getEventType();
            String currentTagAttr = null;
            String currentTitle = null;
            String currentAdvice = null;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();
                if (eventType == XmlPullParser.START_TAG) {
                    if ("tip".equalsIgnoreCase(tagName)) {
                        currentTagAttr = parser.getAttributeValue(null, "tag");
                        currentTitle = null;
                        currentAdvice = null;
                    } else if ("title".equalsIgnoreCase(tagName)) {
                        currentTitle = parser.nextText();
                    } else if ("advice".equalsIgnoreCase(tagName)) {
                        currentAdvice = parser.nextText();
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    if ("tip".equalsIgnoreCase(tagName) && currentTagAttr != null) {
                        TagTip tip = new TagTip(
                                currentTagAttr.toLowerCase().trim(),
                                currentTitle != null ? currentTitle : "Tip for " + currentTagAttr,
                                currentAdvice != null ? currentAdvice : "Focus on basic patterns and edge cases."
                        );
                        map.put(currentTagAttr.toLowerCase().trim(), tip);
                    }
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing tag_tips.xml via XmlPullParser", e);
        }
        return map;
    }

    public static TagTip getTipForTag(Context context, String tag) {
        if (tag == null) return null;
        Map<String, TagTip> map = parseTips(context);
        String key = tag.toLowerCase().trim();
        if (map.containsKey(key)) {
            return map.get(key);
        }
        for (Map.Entry<String, TagTip> entry : map.entrySet()) {
            if (key.contains(entry.getKey()) || entry.getKey().contains(key)) {
                return entry.getValue();
            }
        }
        return new TagTip(tag, tag.toUpperCase() + " Advice",
                "Practice classic problems on this topic, review top solutions, and write out state transitions.");
    }
}

