package org.kagatifoundation.engine.indexer;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import org.kagatifoundation.engine.document.HtmlDocument;
import org.kagatifoundation.engine.document.NewHtmlDocumentObserver;

public class BasicIndexer implements NewHtmlDocumentObserver, AutoCloseable {
    private final Directory directory;
    private final Analyzer analyzer;
    private final IndexWriter writer;

    public BasicIndexer(Path storagePath) throws IOException {
        this.directory = FSDirectory.open(storagePath);
        this.analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        this.writer = new IndexWriter(directory, config);
    }

    @Override
    public void update(HtmlDocument document) {
        System.err.println("Indexing: " + document.getTitle());
        try {
            Document luceneDocument = new Document();
            luceneDocument.add(new TextField("title", document.getTitle(), Store.YES));
            luceneDocument.add(new TextField("url", document.getUrl(), Store.YES));
            writer.addDocument(luceneDocument);
        }
        catch (IOException ioe) {
            System.err.printf("Couldn't index '%s'\n", document.getUrl());
        }
    }

    @Override
    public void close() throws Exception {
        writer.close();
        directory.close();
    }
}