package org.kagatifoundation.engine.crawler;

import org.kagatifoundation.engine.document.HtmlDocument;

public interface HtmlDocumentFetcher {
    HtmlDocument fetch(String link) throws Exception;
}