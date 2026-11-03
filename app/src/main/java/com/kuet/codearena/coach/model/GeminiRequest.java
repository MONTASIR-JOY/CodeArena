package com.kuet.codearena.coach.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Payload model sent to Google Gemini REST API generateContent endpoint.
 */
public class GeminiRequest {

    public static class Part {
        @SerializedName("text")
        private final String text;

        public Part(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

    public static class Content {
        @SerializedName("parts")
        private final List<Part> parts;

        public Content(List<Part> parts) {
            this.parts = parts;
        }

        public List<Part> getParts() {
            return parts;
        }
    }

    @SerializedName("contents")
    private final List<Content> contents;

    public GeminiRequest(String promptText) {
        Part part = new Part(promptText);
        Content content = new Content(Collections.singletonList(part));
        this.contents = Collections.singletonList(content);
    }

    public List<Content> getContents() {
        return contents;
    }
}

