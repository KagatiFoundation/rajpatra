package org.kagati.crawler;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    public CrawledDocument(Document doc, String url) {
        jsoupDocument = doc;
        this.url = url;
        fetchedAt = LocalDateTime.now();
        metadata = extractMetadata();
        clearNoise();
    }

    private Map<String, List<String>> extractMetadata() {
        Map<String, List<String>> metadata = new HashMap<>();
        Elements metaTags = jsoupDocument.select("meta");
        for (Element metaTag: metaTags) {
            String name = metaTag.attr("name");
            String property = metaTag.attr("property");
            String content = metaTag.attr("content");

            if (!name.isEmpty()) {
                name = name.toLowerCase();
                metadata.computeIfAbsent(name, k -> new ArrayList<>()).add(content);
            }
            else if (!property.isEmpty()) {
                property = property.toLowerCase();
                metadata.computeIfAbsent(property, k -> new ArrayList<>()).add(content);
            }
        }
        return metadata;
    }

    private void clearNoise() {
        jsoupDocument.select("header, footer, nav, script, noscript, aside, link").remove();
        // remove ad banners
        jsoupDocument.select("div[class*=ads], div[class*=banner]").remove();
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

    public String getAllContent() {
        StringBuilder builder = new StringBuilder(getMainContent());
        builder.append("\n").append(getArticleContent());
        if (builder.isEmpty()) {
            builder.append(getFallbackContent());
        }
        return builder.toString();
    }

    public String getMainContent() {
        StringBuilder mainContent = new StringBuilder();
        Element mainTag = jsoupDocument.selectFirst("main");
        if (mainTag != null) {
            mainContent.append(mainTag.text()).append("\n");
        }
        return mainContent.toString();
    }

    private String getArticleContent() {
        StringBuilder mainContent = new StringBuilder();
        Elements articleTags = jsoupDocument.select("article");
        for (Element article: articleTags) {
            mainContent.append(article.text()).append("\n");
        }
        return mainContent.toString();
    }

    // heuristic approach
    private String getFallbackContent() {
        StringBuilder content = new StringBuilder();
        Elements divs = jsoupDocument.select("div[id*=content], div[class*=content], section[class*=body]");
        for (Element div : divs) {
            content.append(div.text()).append("\n");
        }
        return content.toString();
    }

    public String getSnippet() {
        String content = getAllContent();
        return content.length() > 200 ? content.substring(0, 200) : content;
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