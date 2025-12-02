package org.kagatifoundation.engine.crawler;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jspecify.annotations.NonNull;
import org.kagatifoundation.engine.document.HtmlDocument;
import org.kagatifoundation.engine.observer.Observer;
import org.kagatifoundation.engine.subject.Subject;

public class BasicCrawler implements Subject {
    private final static Logger LOG = Logger.getLogger(BasicCrawler.class.getName());
    
    private ArrayList<Observer> observers = new ArrayList<>();
    private final CrawlerOptions options;

    private Deque<String> linksToCrawl = new ArrayDeque<>();
    private Set<String> visitedLinks = Collections.synchronizedSet(new HashSet<>());

    private ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public BasicCrawler(CrawlerOptions options) {
        this.options = options;
    }

    public void crawl() {
        linksToCrawl.add(this.options.seedUrl());
        int searchDepth = 0;

        while (searchDepth <= this.options.maxDepth()) {
            int batchSize = linksToCrawl.size();
            if (batchSize == 0) break;

            var crawlTasks = new ArrayList<Future<Optional<HtmlDocument>>>();
            for (int i = 0; i < batchSize; i++) {
                String link = linksToCrawl.pollFirst();
                if (visitedLinks.add(link)) {
                    crawlTasks.add(createLinkCrawlingTask(link));
                }
            }

            for (Future<Optional<HtmlDocument>> crawlTask: crawlTasks) {
                try {
                    Optional<HtmlDocument> htmlDocument = crawlTask.get();
                    if (htmlDocument.isPresent()) {
                        HtmlDocument doc = htmlDocument.get();
                        var anchorTags = doc.getAnchorsTags();
                        if (anchorTags != null) {
                            linksToCrawl.addAll(anchorTags);
                        }
                        notifyObservers(doc);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    BasicCrawler.LOG.log(Level.WARNING, String.format("couldn't create task(%s)", e.getMessage()));
                }
            }
            searchDepth += 1;
        }
    }

    public void shutdown() {
        PlaywrightPageFetcher.shutdown();
    }

    private Future<Optional<HtmlDocument>> createLinkCrawlingTask(@NonNull String link) {
        Future<Optional<HtmlDocument>> task = this.executor.submit(() -> {
            var doc = crawlLink(link);
            return doc;
        });
        return task;
    }

    private Optional<HtmlDocument> crawlLink(@NonNull String link) {
        // try {
        //     var doc = crawlLinkUsingJsoup(link);
        //     return Optional.of(doc);
        // }
        // catch (Exception e) {
        //     BasicCrawler.LOG.log(Level.SEVERE, String.format("(JS) couldn't fetch '%s'", link));
        // }
        try {
           var doc = crawlLinkUsingPlaywright(link);
           return Optional.of(doc);
        }
        catch (Exception e) {
            e.printStackTrace();
            BasicCrawler.LOG.log(Level.SEVERE, String.format("(PY) couldn't fetch '%s'", link));
        }
        return Optional.empty();
    }

    private HtmlDocument crawlLinkUsingJsoup(@NonNull String link) throws Exception {
        var jsoupFetcher = new JsoupPageFetcher();
        var doc = jsoupFetcher.fetch(link);
        return doc;
    }

    private HtmlDocument crawlLinkUsingPlaywright(@NonNull String link) throws Exception {
        var pyFetcher = new PlaywrightPageFetcher();
        var doc = pyFetcher.fetch(link);
        return doc;
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