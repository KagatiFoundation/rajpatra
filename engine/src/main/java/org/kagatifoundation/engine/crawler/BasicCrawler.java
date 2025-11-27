package org.kagatifoundation.engine.crawler;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.jspecify.annotations.NonNull;
import org.kagatifoundation.engine.document.DocumentMetadata;
import org.kagatifoundation.engine.observer.Observer;
import org.kagatifoundation.engine.subject.Subject;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;

public class BasicCrawler implements Subject {
    private ArrayList<Observer> observers = new ArrayList<>();
    private final CrawlerOptions options;

    private Deque<String> linksToCrawl = new ArrayDeque<>();
    private Set<String> visitedLinks = Collections.synchronizedSet(new HashSet<>());

    private ReentrantLock lock = new ReentrantLock();

    public BasicCrawler(CrawlerOptions options) {
        this.options = options;
    }

    public void crawl() {
        this.linksToCrawl.add(this.options.seedUrl());
        int searchDepth = 0;

        while (searchDepth <= this.options.maxDepth()) {
            var searchBatch = new ArrayList<String>(); // links to crawl at once
            lock.lock();

            try {
                while (!linksToCrawl.isEmpty()) {
                    String nextLink = linksToCrawl.pollFirst();
                    if (visitedLinks.add(nextLink)) {
                        searchBatch.add(nextLink);
                    }
                }
            }
            finally {
                lock.unlock();
            }

            searchBatch.forEach(link -> {
                if (link == null) return;
                var page = this.crawlLink(link);
                if (page != null) {
                    var nextBatch = this.prepareLinksForNextBatch(page);
                    this.linksToCrawl.addAll(nextBatch);

                    var meta = new DocumentMetadata(page.title(), link);
                    this.notifyObservers(meta);
                    PlaywrightPageFetcher.closePage(page);
                }
            });
            searchDepth += 1;
        }
        // after the depth has been reached
        PlaywrightPageFetcher.shutdown();
    }

    private Page crawlLink(@NonNull String link) {
        try {
            var html = PlaywrightPageFetcher.fetchPage(link);
            return html;
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    private List<String> prepareLinksForNextBatch(Page page) {
        if (page == null) {
            return new ArrayList<>();
        }
        var links = new ArrayList<String>();
        List<ElementHandle> elements = page.querySelectorAll("a");
        for (ElementHandle anchor: elements) {
            String href = anchor.getAttribute("href");
            if (href != null && !href.isBlank()) {
                String absPath = href.contains("://") ? href : page.url() + href;
                links.add(absPath);
            }
        }
        return links;
    }

    public CrawlerOptions getCrawlerOptions() {
        return options;
    }

    @Override
    public void registerObserver(Observer o) {
        this.observers.add(o);
    }

    @Override
    public void removeObserver(Observer o) {
        this.observers.remove(o);
    }

    @Override
    public void notifyObservers(DocumentMetadata metadata) {
        for (Observer o: this.observers) {
            try {
                o.update(metadata);
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
                break; // document is not valid; ignore it
            }
        }
    }
}