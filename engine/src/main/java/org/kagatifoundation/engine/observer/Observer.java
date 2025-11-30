package org.kagatifoundation.engine.observer;

import org.kagatifoundation.engine.document.HtmlDocument;

public interface Observer {
    void update(HtmlDocument document) throws Exception;
}