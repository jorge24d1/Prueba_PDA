package com.concesionario.dto;

import java.util.List;

public class GeminiRequest {
    private List<Content> contents;

    public static class Content {
        private List<Part> parts;

        // Getters y Setters
        public List<Part> getParts() { return parts; }
        public void setParts(List<Part> parts) { this.parts = parts; }
    }

    public static class Part {
        private String text;

        public Part() {}
        public Part(String text) { this.text = text; }

        // Getters y Setters
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }

    // Getters y Setters
    public List<Content> getContents() { return contents; }
    public void setContents(List<Content> contents) { this.contents = contents; }
}