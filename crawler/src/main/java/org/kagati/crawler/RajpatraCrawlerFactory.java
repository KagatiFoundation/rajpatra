package org.kagati.crawler;

import java.util.List;

import org.kagati.crawler.observer.FetchResultObserver;

import edu.uci.ics.crawler4j.crawler.CrawlController;

public class RajpatraCrawlerFactory implements CrawlController.WebCrawlerFactory<RajpatraCrawler> {
    private final List<FetchResultObserver> sharedObservers;

    public RajpatraCrawlerFactory(List<FetchResultObserver> observers) {
        sharedObservers = observers;
    }

    @Override
    public RajpatraCrawler newInstance() throws Exception {
        RajpatraCrawler crawler = new RajpatraCrawler();
        for (FetchResultObserver observer: sharedObservers) {
            crawler.registerObserver(observer);
        }
        return crawler;
    }
}