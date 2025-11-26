package org.kagatifoundation.engine.document;

import org.kagatifoundation.engine.observer.Observer;

public class DocumentValidator implements Observer {
    @Override
    public void update(DocumentMetadata meta) throws Exception {
        if (meta.url().isEmpty()) {
            throw new DocumentException("Some data is missing from DocumentMetadata");
        }
    }
}