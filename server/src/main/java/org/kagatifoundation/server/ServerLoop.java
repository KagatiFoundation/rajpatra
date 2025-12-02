package org.kagatifoundation.server;

import java.nio.file.Path;
import java.util.logging.Logger;

import org.kagatifoundation.engine.search.BasicIndexSearcher;

import io.javalin.Javalin;

public class ServerLoop {
    private static final Logger LOG = Logger.getLogger(ServerLoop.class.getName());
    private static BasicIndexSearcher INDEX_SEARCHER;

    static {
        try {
            INDEX_SEARCHER = new BasicIndexSearcher(Path.of("/Users/rigelstar/Desktop/KagatiFoundation/rajpatra-data-storage"));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ServerLoop() {
        LOG.info("Starting server...");

        var app = Javalin.create()
            .start(9090);

        LOG.info("Server started!");

        app.get("/", ctx -> {
            String query = ctx.queryParam("query");
            String result = searchUsingQuery(query);
            ctx.json(result);
        });
    }

    private String searchUsingQuery(String query) {
        if (query == null || query.isEmpty()) {
            return "";
        }
        return INDEX_SEARCHER.searchByText(query);
    }
}