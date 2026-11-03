package com.kuet.codearena.coach.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Response model received from Google Gemini REST API generateContent endpoint.
 */
public class GeminiResponse {

    public static class Candidate {
        @SerializedName("content")
        private GeminiRequest.Content content;

        public GeminiRequest.Content getContent() {
            return content;
        }
    }

    @SerializedName("candidates")
    private List<Candidate> candidates;

    public List<Candidate> getCandidates() {
        return candidates;
    }

    public String getFirstText() {
        if (candidates != null && !candidates.isEmpty()) {
            Candidate c = candidates.get(0);
            if (c != null && c.getContent() != null && c.getContent().getParts() != null) {
                List<GeminiRequest.Part> parts = c.getContent().getParts();
                if (!parts.isEmpty() && parts.get(0) != null) {
                    return parts.get(0).getText();
                }
            }
        }
        return null;
    }
}
