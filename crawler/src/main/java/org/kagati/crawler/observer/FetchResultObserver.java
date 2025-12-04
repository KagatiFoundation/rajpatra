package org.kagati.crawler.observer;

import org.kagati.crawler.FetchResult;

public interface FetchResultObserver {
    void update(FetchResult result);
}