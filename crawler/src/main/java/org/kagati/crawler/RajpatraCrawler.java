package org.kagati.crawler;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.kagati.crawler.observer.FetchResultObserver;
import org.kagati.crawler.subject.Subject;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class RajpatraCrawler extends WebCrawler implements Subject {
    private final List<FetchResultObserver> observers;
    private final static Pattern EXCLUSIONS = Pattern.compile(".*(\\.(css|js|xml|gif|jpg|png|mp3|mp4|zip|gz|pdf))$");
    private static final Pattern DOMAIN_PATTERN = Pattern.compile("^https?://([a-z0-9-]+\\.)*gov\\.np(/.*)?", Pattern.CASE_INSENSITIVE);

    public RajpatraCrawler() {
        observers = new ArrayList<>();
    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String urlString = url.getURL().toLowerCase();
        return DOMAIN_PATTERN.matcher(urlString).matches()
            && !EXCLUSIONS.matcher(urlString).matches();
    } 

    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData data = (HtmlParseData) page.getParseData();
            Document jsoupDocument = Jsoup.parse(data.getHtml());
            System.err.println("Found a new HTML page:");
            CrawledDocument crawledData = new CrawledDocument(jsoupDocument, url);
            System.err.println("Content: " + crawledData.getMainBody());
            System.err.println("H1: " + crawledData.getH1());
            System.err.println("Title: " + crawledData.getTitle());
            notifyObservers(crawledData);
        }
    }

    @Override
    public void registerObserver(FetchResultObserver o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(FetchResultObserver o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers(FetchResult r) {
        for (FetchResultObserver o: observers) {
            o.update(r);
        }
    }
}