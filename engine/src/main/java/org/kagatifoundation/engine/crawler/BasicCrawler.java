package org.kagatifoundation.engine.crawler;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;

import org.kagatifoundation.engine.document.DocumentMetadata;
import org.kagatifoundation.engine.observer.Observer;
import org.kagatifoundation.engine.subject.Subject;

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

            List<CompletableFuture<DocumentMetadata>> futures = searchBatch
                .stream()
                .map(link -> CompletableFuture.supplyAsync(() -> {
                    var content = crawlLink(link);
                    var nextBatch = prepareLinksForNextBatch(content);
                    this.linksToCrawl.addAll(nextBatch);

                    var meta = new DocumentMetadata(content, link);
                    notifyObservers(meta);
                    return meta;
                }))
                .toList();

            futures.forEach(CompletableFuture::join);
            searchDepth += 1;
        }
    }

    private String crawlLink(String link) {
        try {
            var html = HtmlFetcher.fetch(link);
            return html;
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    private List<String> prepareLinksForNextBatch(String html) {
        var nextLinks = new ArrayList<String>();
        return nextLinks;
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