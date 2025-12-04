package org.kagati.crawler;

import org.kagati.crawler.html.HtmlDocument;
import org.kagati.crawler.observer.FetchResultObserver;

public interface NewHtmlPageObserver extends FetchResultObserver {
    @Override
    default void update(FetchResult result) {
        HtmlDocument document = (HtmlDocument) result;
        System.err.println(document.title + " " + document.url);
    }
}