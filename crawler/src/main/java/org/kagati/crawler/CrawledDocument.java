package org.kagati.crawler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CrawledDocument implements FetchResult {
    private final Document jsoupDocument;
    private final String url;
    private final LocalDateTime fetchedAt;
    private final Map<String, List<String>> metadata;
    private final String snippet;

    public CrawledDocument(Document doc, String url) {
        jsoupDocument = doc;
        this.url = url;
        fetchedAt = LocalDateTime.now();
        metadata = extractMetadata();
        snippet = generateSnippet();
    }

    private Map<String, List<String>> extractMetadata() {
        Map<String, List<String>> metadata = new HashMap<>();
        Elements metaTags = jsoupDocument.select("meta");
        for (Element metaTag: metaTags) {
            String name = metaTag.attr("name");
            String property = metaTag.attr("property");
            String content = metaTag.attr("content");

            if (!name.isEmpty()) {
                if (metadata.containsKey(name)) {
                    metadata.get(name).add(content);
                }
                else {
                    metadata.put(name.toLowerCase(), List.of(content));
                }
            }
            else if (!property.isEmpty()) {
                if (metadata.containsKey(property)) {
                    metadata.get(property).add(content);
                }
                else {
                    metadata.put(property.toLowerCase(), List.of(content));
                }
            }
        }
        return metadata;
    }

    private String generateSnippet() {
        return "";
    }

    /**
     * @return A string containing text from every `h1` element(separated by newlines) of the page.
     */
    public String getMainHeading() {
        StringBuilder builder = new StringBuilder();
        Elements headingElems = jsoupDocument.select("h1");
        for (Element element: headingElems) {
            builder.append(element.text()).append("\n");
        }
        return builder.toString();
    }

    public String getTitle() {
        return jsoupDocument.title();
    }

    public String getKeywords() {
        List<String> keywords = metadata.get("keywords");
        return String.join(",", keywords);
    }

    public String getOgTitle() {
        var ogTitle = metadata.get("og:title");
        if (ogTitle != null && ogTitle.get(0) != null) return ogTitle.get(0);
        else return getTitle();
    }

    public String getOgImage() {
        var ogImages = metadata.get("og:image");
        if (ogImages != null && ogImages.get(0) != null) return ogImages.get(0);
        else return "";
    }

    public String getOgType() {
        var ogTypes = metadata.get("og:type");
        if (ogTypes != null && ogTypes.get(0) != null) return ogTypes.get(0);
        else return "website"; // always fallback to 'website'
    }

    public String getSnippet() {
        return snippet;
    }

    public List<String> getMetadata(String name) {
        return metadata.get(name);
    }

    public String getUrl() {
        return url;
    }

    public LocalDateTime getFetchTime() {
        return fetchedAt;
    }
}