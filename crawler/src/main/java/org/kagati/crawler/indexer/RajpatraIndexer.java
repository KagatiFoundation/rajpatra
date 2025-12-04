package org.kagati.crawler.indexer;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.kagati.crawler.FetchResult;
import org.kagati.crawler.NewHtmlPageObserver;
import org.kagati.crawler.html.HtmlDocument;

public class RajpatraIndexer implements NewHtmlPageObserver, AutoCloseable {
    private final Directory directory;
    private final Analyzer analyzer;
    private final IndexWriter writer;

    public RajpatraIndexer(Path storagePath) throws IOException {
        this.directory = FSDirectory.open(storagePath);
        this.analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        this.writer = new IndexWriter(directory, config);
    }

    @Override
    public void update(FetchResult result) {
        HtmlDocument document = (HtmlDocument) result;
        try {
            Document luceneDocument = new Document();
            TextField titleField = new TextField("title", document.title, Store.YES);
            var contentField = new TextField("content", document.title, Store.NO);
            var urlField = new StringField("url", document.url, Store.YES);
            luceneDocument.add(titleField);
            luceneDocument.add(contentField);
            luceneDocument.add(urlField);
            writer.addDocument(luceneDocument);
        }
        catch (IOException ioe) {
            System.err.printf("Couldn't index '%s'\n", document.url);
        }
    }

    @Override
    public void close() throws Exception {
        writer.commit();
        writer.close();
        directory.close();
    }
}