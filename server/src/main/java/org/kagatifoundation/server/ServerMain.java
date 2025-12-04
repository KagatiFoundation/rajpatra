package org.kagatifoundation.server;

import org.kagati.crawler.search.RajpatraSearcher;

public class ServerMain {
    public static void main(String[] args) {
        if (args.length > 0) {
            if (args[0].equals("index")) {
                System.err.println("Indexing...");
                ServerLoop.start();
            }
            else if (args[0].equals("search")) {
                System.err.println("Searching...");
                try(var searcher = new RajpatraSearcher(null)) {
                    var result = searcher.searchByText("python");
                    System.err.println(result);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        else {
            System.err.println("Nothing! Bye!");
        }
    }
}