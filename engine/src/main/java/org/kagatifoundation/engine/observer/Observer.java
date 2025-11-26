package org.kagatifoundation.engine.observer;

import org.kagatifoundation.engine.document.DocumentMetadata;

public interface Observer {
    void update(DocumentMetadata meta) throws Exception;
}