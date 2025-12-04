package org.kagati.crawler.html;

import org.kagati.crawler.FetchResult;

public class HtmlDocument implements FetchResult {
    public String title;
    public String url;

    public HtmlDocument(String title, String url) {
        this.title = title;
        this.url = url;
    }
}