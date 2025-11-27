package org.kagatifoundation.server;

import org.kagatifoundation.engine.crawler.BasicCrawler;
import org.kagatifoundation.engine.crawler.CrawlerOptions;
import org.kagatifoundation.engine.document.DocumentValidator;
import org.kagatifoundation.engine.indexer.BasicIndexer;

public class ServerMain {
    public static void main(String[] args) {
        var crawler = new BasicCrawler(new CrawlerOptions("https://mofa.gov.np/", 1, false));
        crawler.registerObserver(new DocumentValidator());
        crawler.registerObserver(new BasicIndexer());
        crawler.crawl();
    }    
}