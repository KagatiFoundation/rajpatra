package org.kagatifoundation.engine.document;

import java.net.URI;
import java.net.URISyntaxException;
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
        String baseUrl = page.url();

        for (ElementHandle anchor: elements) {
            String href = anchor.getAttribute("href");
            if (href != null) {
                href = href.strip();
                if (href.isEmpty()) continue;

                try {
                    URI hrefUri = new URI(href);
                    if (hrefUri.isAbsolute()) {
                        links.add(hrefUri.toString());
                    } else {
                        URI baseUri = new URI(baseUrl);
                        URI resolved = baseUri.resolve(hrefUri);
                        links.add(resolved.toString());
                    }
                } catch (URISyntaxException e) {
                    System.err.println("Invalid href: " + href + " on page " + baseUrl);
                }
            }
        }
        anchorsTags = links;
        return this;
    }

    public HtmlDocument build() {
        var document = new HtmlDocument(page.title(), page.url(), page.content());
        document.setAnchorTags(this.anchorsTags);
        return document;
    }
}