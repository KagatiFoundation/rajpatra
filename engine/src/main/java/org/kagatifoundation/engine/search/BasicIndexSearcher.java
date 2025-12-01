package org.kagatifoundation.engine.search;

import java.nio.file.Path;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import org.apache.lucene.queryparser.classic.QueryParser;

public class BasicIndexSearcher {
    private final Directory directory;
    private final IndexSearcher searcher;
    private final QueryParser queryParser;

    public BasicIndexSearcher(Path indexPath) throws Exception {
        this.directory = FSDirectory.open(Path.of("/Users/rigelstar/Desktop/KagatiFoundation/rajpatra-data-storage"));
        IndexReader reader = DirectoryReader.open(directory);
        this.searcher = new IndexSearcher(reader);
        StandardAnalyzer analyzer = new StandardAnalyzer();
        this.queryParser = new QueryParser("title", analyzer);
    }

    public String searchByText(String text) {
        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append("{");
        try {
            IndexReader reader = DirectoryReader.open(directory);
            Query query = queryParser.parse(text);
            TopDocs results = searcher.search(query, 10); // top 10

            var counter = 0;
            for (ScoreDoc sd : results.scoreDocs) {
                Document document = searcher.storedFields().document(sd.doc);
                String title = document.get("title");
                String url = document.get("url");
                if (counter != 0) {
                    resultBuilder.append(",");
                }
                resultBuilder.append(String.format("\"%d\": {\"title\": \"%s\", \"url\": \"%s\"}", counter, title, url));
                counter += 1;
            }
            reader.close(); 

            resultBuilder.append("}");
            return resultBuilder.toString();
        }
        catch (Exception ioe) {
            ioe.printStackTrace();
            return null;
        }
    }
}