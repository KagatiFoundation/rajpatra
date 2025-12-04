package org.kagatifoundation.server;

import java.nio.file.Path;
import java.util.List;

import org.kagati.crawler.CrawlerRuntime;
import org.kagati.crawler.RajpatraCrawlerConfig;
import org.kagati.crawler.indexer.RajpatraIndexer;

public class ServerLoop {
    public static void start() {
        RajpatraCrawlerConfig config = new RajpatraCrawlerConfig(
            4, 
            1,
            new String[] { "https://nepalpassport.gov.np", "https://mofa.gov.np" }, 
            "src/test/resources/rajpatra-crawler"
        );

        CrawlerRuntime crawlerRuntime = new CrawlerRuntime(config);
        try(var indexer = new RajpatraIndexer(Path.of("src/test/resources/rajpatra-index-data"))) {
            crawlerRuntime.startCrawler(List.of(indexer));
        }
        catch (Exception e) {
            e.printStackTrace();        
        }
    }
}