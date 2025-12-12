package org.kagati.crawler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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

        removeNoise();
        metadata = extractMetadata();
    }

    private Map<String, List<String>> extractMetadata() {
        Map<String, List<String>> map = new HashMap<>();
        Elements metaTags = jsoupDocument.select("meta");

        for (Element meta : metaTags) {
            String key = meta.hasAttr("name")
                    ? meta.attr("name")
                    : meta.attr("property");

            if (!key.isEmpty()) {
                key = key.toLowerCase();
                map.computeIfAbsent(key, k -> new ArrayList<>())
                    .add(meta.attr("content"));
            }
        }
        return map;
    }

    private void removeNoise() {
        jsoupDocument.select(
                "div[id*=menu], div[class*=menu],"
                        + "div[id*=nav], div[class*=nav],"
                        + "div[id*=sidebar], div[class*=sidebar],"
                        + "div[id*=breadcrumb], div[class*=breadcrumb]"
        ).remove();
        jsoupDocument.select("script, noscript, style, iframe, header, footer, nav, aside, link").remove();
        jsoupDocument.select("ul, li, form, button, input, svg, figure, picture").remove();
        jsoupDocument.select("table").remove();
        jsoupDocument.select("div[class*=ads], div[class*=banner]").remove();
    }

    public String getTitle() {
        String og = getMeta("og:title").stream().findFirst().orElse("");
        return !og.isEmpty() ? og : jsoupDocument.title();
    }

    public String getH1() {
        return jsoupDocument.select("h1").eachText().stream()
            .collect(Collectors.joining("\n"));
    }

    public String getMainBody() {
        Element main = jsoupDocument.selectFirst("main");
        if (main != null) {
            return normalize(main.text());
        }

        Elements articles = jsoupDocument.select("article");
        if (!articles.isEmpty()) {
            return normalize(articles.text());
        }

        Elements divs = jsoupDocument.select("div[id*=content], div[class*=content], section[class*=body]");
        if (!divs.isEmpty()) {
            return normalize(divs.text());
        }
        return normalize(jsoupDocument.body().text());
    }

    private String normalize(String text) {
        if (text == null) return "";
        text = text.replaceAll("\\s+", " ").trim();
        return text;
    }

    public String getKeywords() {
        List<String> keywords = metadata.get("keywords");
        return String.join(",", keywords);
    }

    /**
     * @return og:site_name
     */
    public String getOgSiteName() {
        var ogSiteName = metadata.get("og:site_name");
        if (ogSiteName != null && ogSiteName.get(0) != null) return ogSiteName.get(0);
        else return "";
    }

    public String getOgPublishedTime() {
        var ogPt = metadata.get("og:published_time");
        if (ogPt != null && ogPt.get(0) != null) return ogPt.get(0);
        else return "";
    }

    public String getAllContent() {
        Set<String> seen = new LinkedHashSet<>();

        Consumer<String> add = s -> {
            if (s != null && !s.isBlank()) seen.add(s);
        };

        add.accept(getH1());
        add.accept(getMainBody());
        return String.join("\n", seen);
    }

    public String getSnippet() {
        String text = getMainBody();
        return text.length() > 200 ? text.substring(0, 200) : text;
    }

    public String getUrl() {
        return url;
    }

    public String getOgImage() {
        return getMeta("og:image").stream().findFirst().orElse("");
    }

    public String getOgType() {
        return getMeta("og:type").stream().findFirst().orElse("website");
    }

    public String getOgTitle() {
        return getMeta("og:title").stream().findFirst().orElse(getTitle());
    }

    public List<String> getMeta(String name) {
        return metadata.getOrDefault(name, Collections.emptyList());
    }

    public LocalDateTime getFetchTime() {
        return fetchedAt;
    }
}