package org.kagatifoundation.engine.crawler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HtmlFetcher {
    public static String fetch(String urlToFetch) throws Exception {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlToFetch);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))
        ) {
            for (String line; (line = reader.readLine()) != null; ) {
                result.append(line);
            }
        }
        return result.toString();
    }    
}