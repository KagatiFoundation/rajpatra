package org.kagatifoundation.engine.crawler;

public record CrawlerOptions(
    String seedUrl, 
    int maxDepth,
    boolean visitExternalLinks
) {}