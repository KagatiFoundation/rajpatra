package org.kagatifoundation.engine.document;

import java.util.List;

/**
 * {@code HtmlDocument} facilitates with utility methods which are 
 * useful for crawling, indexing, and searcing.
 */
public class HtmlDocument {
    private List<String> anchorTags;
    private String title;
    private String url;
    private String content;

    public HtmlDocument(String title, String url, String content) {
        this.title = title;
        this.url = url;
        this.content = content;
    }

    public void setAnchorTags(List<String> tags) {
        this.anchorTags = tags;
    }

    public List<String> getAnchorsTags() {
        return anchorTags;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getContent() {
        return content;
    }
}