package org.kagati.crawler.subject;

import org.kagati.crawler.FetchResult;
import org.kagati.crawler.observer.FetchResultObserver;

public interface Subject {
    public void registerObserver(FetchResultObserver o);
    public void removeObserver(FetchResultObserver o);
    public void notifyObservers(FetchResult r);
}