package org.kagatifoundation.engine.subject;

import org.kagatifoundation.engine.document.DocumentMetadata;
import org.kagatifoundation.engine.observer.Observer;

public interface Subject {
    void registerObserver(Observer o);
    void removeObserver(Observer o);
    void notifyObservers(DocumentMetadata metadata);
}