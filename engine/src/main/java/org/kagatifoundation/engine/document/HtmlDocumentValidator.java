package org.kagatifoundation.engine.document;

public class HtmlDocumentValidator implements NewHtmlDocumentObserver {
    @Override
    public void update(HtmlDocument document) throws Exception {
        if (document.getUrl().isEmpty()) {
            throw new DocumentException("Some data is missing from DocumentMetadata");
        }
    }
}