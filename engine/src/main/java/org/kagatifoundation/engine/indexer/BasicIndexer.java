package org.kagatifoundation.engine.indexer;

import org.kagatifoundation.engine.document.DocumentMetadata;
import org.kagatifoundation.engine.observer.Observer;

public class BasicIndexer implements Observer {
    @Override
    public void update(DocumentMetadata meta) {
        System.out.printf("URL: %s\n", meta.url());   
    }
}