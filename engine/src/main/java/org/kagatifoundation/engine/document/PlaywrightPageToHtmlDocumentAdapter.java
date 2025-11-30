package org.kagatifoundation.engine.document;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.NonNull;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;

public class PlaywrightPageToHtmlDocumentAdapter {
    public static HtmlDocument toHtmlDocument(@NonNull Page page) {
        var document = new HtmlDocumentBuilder().from(page).parseAnchorTags().build();
        return document;
    }    
}

class HtmlDocumentBuilder {
    private Page page;
    private List<String> anchorsTags;

    protected HtmlDocumentBuilder from(@NonNull Page page) {
        this.page = page;
        return this;
    }

    protected HtmlDocumentBuilder parseAnchorTags() {
        var links = new ArrayList<String>();
        List<ElementHandle> elements = page.querySelectorAll("a");
        for (ElementHandle anchor: elements) {
            String href = anchor.getAttribute("href");
            if (href != null && !href.isBlank()) {
                String absPath = href.contains("://") ? href : page.url() + href;
                links.add(absPath);
            }
        }
        anchorsTags = links;
        return this;
    }

    public HtmlDocument build() {
        var document = new HtmlDocument(page.title(), page.url(), new String());
        document.setAnchorTags(this.anchorsTags);
        return document;
    }
}