package org.kagatifoundation.engine.crawler;

import java.util.logging.Logger;

public class CrawlerRuntime {
    private final CrawlerOptions options;
    private final BasicCrawler crawler;

    private final static Logger LOG = Logger.getLogger(BasicCrawler.class.getName());

    public CrawlerRuntime(CrawlerOptions opts) {
        options = opts;
        crawler = new BasicCrawler(opts);
    }

    public void start() {
        crawler.crawl();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                LOG.info("Shutting down crawler along with all of its Playwright instances...");
                crawler.shutdown();
                LOG.info("Shutdown successful!");
            }
        });
    }

    // manual shutdown
    public void shutdown() {
        crawler.shutdown();
    }

    public BasicCrawler crawler() {
        return crawler;
    }

    public CrawlerOptions getOptions() {
        return options;
    }
}