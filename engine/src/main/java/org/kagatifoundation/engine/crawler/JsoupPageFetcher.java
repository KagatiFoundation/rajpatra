package org.kagatifoundation.engine.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.kagatifoundation.engine.document.HtmlDocument;

public class JsoupPageFetcher implements HtmlDocumentFetcher {
    @Override
    public HtmlDocument fetch(String link) throws Exception {
        Document doc = Jsoup.connect(link).get();
        HtmlDocument htmlDoc = new HtmlDocument(doc.title(), doc.baseUri(), doc.text());
        return htmlDoc;
    }
}