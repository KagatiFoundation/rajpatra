package org.kagati.crawler.html;

import org.kagati.crawler.FetchResult;

public class HtmlDocument implements FetchResult {
    public String title;
    public String url;
    public String content;

    public HtmlDocument(String title, String url, String content) {
        this.title = title;
        this.url = url;
        this.content = content;
    }
}