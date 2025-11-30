package org.kagatifoundation.engine.crawler;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

import org.jspecify.annotations.NonNull;
import org.kagatifoundation.engine.document.HtmlDocument;
import org.kagatifoundation.engine.document.PlaywrightPageToHtmlDocumentAdapter;
import org.kagatifoundation.engine.observer.Observer;
import org.kagatifoundation.engine.subject.Subject;

public class BasicCrawler implements Subject {
    private ArrayList<Observer> observers = new ArrayList<>();
    private final CrawlerOptions options;

    private Deque<String> linksToCrawl = new ArrayDeque<>();
    private Set<String> visitedLinks = Collections.synchronizedSet(new HashSet<>());

    private ReentrantLock lock = new ReentrantLock();

    private ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

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

            var crawlTasks = new ArrayList<Future<HtmlDocument>>();
            for (String linkToCrawl: searchBatch) {
                if (linkToCrawl != null) {
                    var crawlTask = createLinkCrawlingTask(linkToCrawl);
                    crawlTasks.add(crawlTask);
                }
            }

            for (Future<HtmlDocument> crawlTask: crawlTasks) {
                try {
                    HtmlDocument htmlDocument = crawlTask.get();
                    notifyObservers(htmlDocument);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

            searchDepth += 1;
        }
        // after the depth has been reached
        PlaywrightPageFetcher.shutdown();
    }

    private Future<HtmlDocument> createLinkCrawlingTask(@NonNull String link) {
        Future<HtmlDocument> pageFetchTask = this.executor.submit(() -> {
            var crawlTask = crawlLink(link);
            return crawlTask;
        });
        return pageFetchTask;
    }

    private HtmlDocument crawlLink(@NonNull String link) {
        try {
            var pwPage = PlaywrightPageFetcher.fetchPage(link);
            if (pwPage != null) {
                var htmlDocument = PlaywrightPageToHtmlDocumentAdapter.toHtmlDocument(pwPage);
                PlaywrightPageFetcher.closePage(pwPage);
                return htmlDocument;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
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
    public void notifyObservers(HtmlDocument document) {
        for (Observer o: this.observers) {
            try {
                o.update(document);
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
                break; // document is not valid; ignore it
            }
        }
    }
}