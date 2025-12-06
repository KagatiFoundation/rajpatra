package org.kagatifoundation.server;

import java.nio.file.Path;

import org.kagati.crawler.search.RajpatraSearcher;

import io.javalin.Javalin;

public class ServerMain {
    public static void main(String[] args) {
        if (args.length > 0) {
            if (args[0].equals("i")) {
                System.err.println("Indexing...");
                ServerLoop.start();
            }
            else if (args[0].equals("search")) {
                System.err.println("Searching...");
                var searcher = new SearchLoop();
                searcher.start();
            }
        }
        System.err.println("Nothing! Bye!");
    }
}

class SearchLoop implements AutoCloseable {
    private RajpatraSearcher searcher;

    public SearchLoop() {
        try {
            searcher = new RajpatraSearcher(Path.of("src/test/resources/rajpatra-index-data"));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        var server = Javalin.create(config -> {
                config.bundledPlugins.enableCors(cors -> {
                    cors.addRule(it -> {
                        it.anyHost();
                    });
                });
            })
            .get("/search", ctx -> {
                String query = ctx.queryParam("q");
                System.out.println("QUERY: " + query);
                if (query == null) {
                    ctx.json("{}");
                }
                else {
                    ctx.json(searchByText(query));
                }
            })
            .start(8080);
    }

    @Override
    public void close() throws Exception {
        if (searcher == null) return;
        searcher.close();
    }

    public String searchByText(String text) {
        if (searcher == null) return null;
        return searcher.searchByText(text);
    }
}