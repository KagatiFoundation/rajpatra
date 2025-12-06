package org.kagati.crawler.search;

import java.nio.file.Path;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.document.Document;

public class RajpatraSearcher implements AutoCloseable {
    private final Directory directory;
    private final IndexReader indexReader;
    private final IndexSearcher searcher;
    private final StandardAnalyzer analyzer;

    public RajpatraSearcher(Path indexPath) throws Exception {
        this.directory = FSDirectory.open(Path.of("src/test/resources/rajpatra-index-data"));
        this.indexReader = DirectoryReader.open(directory);
        IndexReader reader = DirectoryReader.open(directory);
        this.searcher = new IndexSearcher(reader);
        this.analyzer = new StandardAnalyzer();
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