package org.kagati.crawler;

public record RajpatraCrawlerConfig(
    int numCrawlers, 
    int maxDepth,
    String[] seedUrls,
    String storage
) {}