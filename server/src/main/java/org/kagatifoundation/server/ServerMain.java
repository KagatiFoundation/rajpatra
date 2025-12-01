package org.kagatifoundation.server;

import java.nio.file.Path;

import org.kagatifoundation.engine.crawler.BasicCrawler;
import org.kagatifoundation.engine.crawler.CrawlerOptions;
import org.kagatifoundation.engine.document.HtmlDocumentValidator;
import org.kagatifoundation.engine.indexer.BasicIndexer;

public class ServerMain {
    public static void main2(String[] args) {
        var crawler = new BasicCrawler(new CrawlerOptions("https://nepalpassport.gov.np/", 1, false));
        crawler.registerObserver(new HtmlDocumentValidator());
        try(BasicIndexer indexer = new BasicIndexer(Path.of("/Users/rigelstar/Desktop/KagatiFoundation/rajpatra-data-storage"))) {
            crawler.registerObserver(indexer);
            crawler.crawl();
        }
        catch (Exception ioe) {
            ioe.printStackTrace();
        }
    }

    public static void main(String[] args) {
        var serverLoop = new ServerLoop();
    }
}