package org.kagati.crawler.indexer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.kagati.crawler.CrawledDocument;
import org.kagati.crawler.FetchResult;
import org.kagati.crawler.NewHtmlPageObserver;

public class RajpatraIndexer implements NewHtmlPageObserver, AutoCloseable {
    private final Directory directory;
    private final Analyzer analyzer;
    private final IndexWriter writer;

    private final static Logger LOG = Logger.getLogger(RajpatraIndexer.class.getName());

    public RajpatraIndexer(Path storagePath) throws IOException {
        this.directory = FSDirectory.open(storagePath);
        this.analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        this.writer = new IndexWriter(directory, config);
    }

    @Override
    public void update(FetchResult result) {
        if (!(result instanceof CrawledDocument)) {
            LOG.log(Level.WARNING, "RajpatraIndexer cannot process the given FetchResult! Aborting...");
            return;
        }

        CrawledDocument document = (CrawledDocument) result;
        try {
            Document luceneDocument = new Document();
            luceneDocument.add(new TextField("title", document.getTitle(), Store.YES));
            luceneDocument.add(new TextField("heading", document.getH1(), Store.YES));
            luceneDocument.add(new TextField("main_conent", document.getMainBody(), Store.YES));
            luceneDocument.add(new TextField("site_name", document.getOgSiteName(), Store.YES));

            // url structure as searchable tokens
            luceneDocument.add(new TextField("url_tokens", tokenizeUrl(document.getUrl()), Store.NO));
            luceneDocument.add(new StringField("url", document.getUrl(), Store.YES));
            luceneDocument.add(new StoredField("snippet", document.getSnippet()));
            luceneDocument.add(new StoredField("image", document.getOgImage()));
            luceneDocument.add(new StoredField("type", document.getOgType()));

            double alpha = 0.25;
            double depthBoost = 1.0 + alpha * Math.log1p(computeUrlDepth(document.getUrl()));
            luceneDocument.add(new DoubleDocValuesField("url_depth", depthBoost));
            writer.addDocument(luceneDocument);
        }
        catch (IOException ioe) {
            System.err.printf("Couldn't index '%s'\n", document.getUrl());
        }
    }

    private String tokenizeUrl(String url) {
        return url.replace("https://", "")
            .replace("http://", "")
            .replaceAll("[/_.-]", " ");
    }

    private double computeUrlDepth(String url) {
        return url.chars().filter(c -> c == '/').count();
    }

    @Override
    public void close() throws Exception {
        writer.commit();
        writer.close();
        directory.close();
    }
}