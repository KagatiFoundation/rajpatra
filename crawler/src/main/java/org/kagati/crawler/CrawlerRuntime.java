package org.kagati.crawler;

import java.io.File;
import java.util.List;

import org.kagati.crawler.observer.FetchResultObserver;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class CrawlerRuntime {
    private RajpatraCrawlerConfig config;

    public CrawlerRuntime(RajpatraCrawlerConfig config) {
        this.config = config;
    }    

    public void startCrawler(List<FetchResultObserver> observers) {
        File crawlStorage = new File(config.storage());
        CrawlConfig cConfig = new CrawlConfig();
        cConfig.setCrawlStorageFolder(crawlStorage.getAbsolutePath());
        cConfig.setMaxDepthOfCrawling(config.maxDepth());
        cConfig.setPolitenessDelay(500);
        cConfig.setUserAgentString("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36");
        cConfig.setMaxPagesToFetch(1000);
        cConfig.setMaxOutgoingLinksToFollow(100);

        PageFetcher pageFetcher = new PageFetcher(cConfig);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);

        try {
            CrawlController crawlController = new CrawlController(cConfig, pageFetcher, robotstxtServer);
            for (String seed: config.seedUrls()) {
                crawlController.addSeed(seed);
            }
            RajpatraCrawlerFactory factory = new RajpatraCrawlerFactory(observers);
            crawlController.start(factory, config.numCrawlers());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}