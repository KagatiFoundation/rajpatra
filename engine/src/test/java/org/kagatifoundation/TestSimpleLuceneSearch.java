package org.kagatifoundation;

import java.nio.file.Path;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import org.apache.lucene.queryparser.classic.QueryParser;

import org.junit.Test;

public class TestSimpleLuceneSearch {
    @Test
    public void testSearchInLuceneUsingSomeRandomText() {
        String text = "national id";
        try {
            Directory directory = FSDirectory.open(Path.of("/Users/rigelstar/Desktop/KagatiFoundation/rajpatra-data-storage"));
            IndexReader reader = DirectoryReader.open(directory);
            IndexSearcher searcher = new IndexSearcher(reader);

            StandardAnalyzer analyzer = new StandardAnalyzer();
            QueryParser parser = new QueryParser("title", analyzer);

            Query query = parser.parse(text);

            // Search top 10
            TopDocs results = searcher.search(query, 10);
            System.out.println("Hits: " + results.totalHits);

            for (ScoreDoc sd : results.scoreDocs) {
                System.out.println("Title: " + sd.score);
            }
            reader.close(); 
        }
        catch (Exception ioe) {
            ioe.printStackTrace();
        }
    }    
}