package com.concesionario.dto;

import java.util.List;

public class GeminiResponse {
    private List<Candidate> candidates;

    public static class Candidate {
        private Content content;
        private String finishReason;

        // Getters y Setters
        public Content getContent() { return content; }
        public void setContent(Content content) { this.content = content; }
        public String getFinishReason() { return finishReason; }
        public void setFinishReason(String finishReason) { this.finishReason = finishReason; }
    }

    public static class Content {
        private List<Part> parts;
        private String role;

        // Getters y Setters
        public List<Part> getParts() { return parts; }
        public void setParts(List<Part> parts) { this.parts = parts; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    public static class Part {
        private String text;

        // Getters y Setters
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }

    // Getters y Setters
    public List<Candidate> getCandidates() { return candidates; }
    public void setCandidates(List<Candidate> candidates) { this.candidates = candidates; }
}