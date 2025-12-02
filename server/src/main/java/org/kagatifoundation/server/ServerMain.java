package org.kagatifoundation.server;

import java.nio.file.Path;

import org.kagatifoundation.engine.crawler.CrawlerOptions;
import org.kagatifoundation.engine.crawler.CrawlerRuntime;
import org.kagatifoundation.engine.document.HtmlDocumentValidator;
import org.kagatifoundation.engine.indexer.BasicIndexer;

public class ServerMain {
    public static void main(String[] args) {
        // var crawlerRuntime = new CrawlerRuntime(new CrawlerOptions("https://python.org/", 1, false));
        var crawlerRuntime = new CrawlerRuntime(new CrawlerOptions("https://nepalpassport.gov.np/", 0, false));
        crawlerRuntime.crawler().registerObserver(new HtmlDocumentValidator());
        try(BasicIndexer indexer = new BasicIndexer(Path.of("/Users/rigelstar/Desktop/KagatiFoundation/rajpatra-data-storage"))) {
            crawlerRuntime.crawler().registerObserver(indexer);
            crawlerRuntime.start();
        }
        catch (Exception ioe) {
            ioe.printStackTrace();
        }
        finally {
            crawlerRuntime.shutdown();
        }
    }

    public static void main2(String[] args) {
        var serverLoop = new ServerLoop();
    }
}