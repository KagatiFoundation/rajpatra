package org.kagati.crawler.search;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.document.Document;

import org.kagati.crawler.entity.Ministry;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class RajpatraSearcher implements AutoCloseable {
    private final static Logger LOG = Logger.getLogger(RajpatraSearcher.class.getName());

    private final Directory directory;
    private final IndexReader indexReader;
    private final IndexSearcher searcher;
    private final StandardAnalyzer analyzer;

    private final Map<String, Ministry> allMinistries;

    public RajpatraSearcher(Path indexPath) throws Exception {
        LOG.info("setting up the searcher...");
        LOG.info("loading ministries...");
        allMinistries = loadMinistries();
        if (allMinistries == null) {
            LOG.log(Level.SEVERE, "couldn't load ministries! Aborting...");
            System.exit(1);
        }
        else {
            LOG.info("ministries loaded!");
        }

        this.directory = FSDirectory.open(Path.of("src/test/resources/rajpatra-index-data"));
        this.indexReader = DirectoryReader.open(directory);
        IndexReader reader = DirectoryReader.open(directory);
        this.searcher = new IndexSearcher(reader);
        this.analyzer = new StandardAnalyzer();
        LOG.info("searcher is set up...");
    }

    private Map<String, Ministry> loadMinistries() {
        ObjectMapper mapper = new ObjectMapper();
        try (var is = RajpatraSearcher.class.getClassLoader().getResourceAsStream("ministries.json")) {
            List<Ministry> ministries = mapper.readValue(
                is,
                new TypeReference<>() {}
            );
            var allMinistries = ministries
                .stream()
                .collect(Collectors.toMap(Ministry::id, m -> m));

            return allMinistries;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String searchUsingTextAndDepartment(SearchQuery searchQuery) {
        Ministry ministry = allMinistries.get(searchQuery.ministry());
        if (ministry == null) {
            return searchByText(searchQuery.query());
        }

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode rootObject = objectMapper.createObjectNode();
        try {
            Query titleQuery = new QueryParser("title", analyzer).parse(searchQuery.query());
            Query contentQuery = new QueryParser("content", analyzer).parse(searchQuery.query());
            Query finalQuery = new BooleanQuery.Builder()
                .add(new BoostQuery(titleQuery, 3.0f), BooleanClause.Occur.SHOULD)
                .add(new BoostQuery(contentQuery, 1.0f), BooleanClause.Occur.SHOULD)
                .build();

            TopDocs results = searcher.search(finalQuery, 10); // top 10

            var counter = 0;
            for (ScoreDoc sd : results.scoreDocs) {
                Document document = searcher.storedFields().document(sd.doc);
                String title = document.get("title");
                String url = document.get("url");
                if (!url.contains(ministry.domain().strip())) {
                    continue;    
                }
                ObjectNode item = objectMapper.createObjectNode();
                item.put("title", title);
                item.put("url", url);
                rootObject.set(String.format("%d", counter), item);
                counter += 1;
            }
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootObject);
        }
        catch (Exception ioe) {
            ioe.printStackTrace();
            return null;
        }
    }

    public String searchByText(String text) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode rootObject = objectMapper.createObjectNode();

        try {
            Query titleQuery = new QueryParser("title", analyzer).parse(text);
            Query contentQuery = new QueryParser("content", analyzer).parse(text);
            Query finalQuery = new BooleanQuery.Builder()
                .add(new BoostQuery(titleQuery, 3.0f), BooleanClause.Occur.SHOULD)
                .add(new BoostQuery(contentQuery, 1.0f), BooleanClause.Occur.SHOULD)
                .build();

            TopDocs results = searcher.search(finalQuery, 10); // top 10

            var counter = 0;
            for (ScoreDoc sd : results.scoreDocs) {
                Document document = searcher.storedFields().document(sd.doc);
                String title = document.get("title");
                String url = document.get("url");
                ObjectNode item = objectMapper.createObjectNode();
                item.put("title", title);
                item.put("url", url);
                rootObject.set(String.format("%d", counter), item);
                counter += 1;
            }
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootObject);
        }
        catch (Exception ioe) {
            ioe.printStackTrace();
            return null;
        }
    }

    @Override
    public void close() throws Exception {
        indexReader.close();
        directory.close();
    }    
}