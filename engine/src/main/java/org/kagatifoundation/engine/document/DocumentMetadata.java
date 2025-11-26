package org.kagatifoundation.engine.document;

public record DocumentMetadata(String title, String url) {
    public DocumentMetadata {
        if (title == null || title.isEmpty() || url == null || url.isEmpty()) {
            System.out.println("WARNING: some data is missing in DocumentMetadata");
        }
    }
}