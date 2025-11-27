package org.kagatifoundation.engine.crawler;

import java.util.Arrays;

import org.jspecify.annotations.NonNull;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.WaitUntilState;

public class PlaywrightPageFetcher {
    private static final Playwright PLAYWRIGHT;
    private static final Browser BROWSER;

    static {
        PLAYWRIGHT = Playwright.create();
        BROWSER = PLAYWRIGHT.chromium().launch(
            new BrowserType.LaunchOptions()
                .setHeadless(true)
                .setArgs(Arrays.asList("--no-sandbox", "--disable-dev-shm-usage"))
        );
    } 

    public static Page fetchPage(@NonNull String urlToFetch) throws Exception {
        Browser.NewContextOptions ctxOptions = new Browser.NewContextOptions()
            .setUserAgent(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36"
            )
            .setViewportSize(1280, 800)
            .setIgnoreHTTPSErrors(true);

        BrowserContext ctx = BROWSER.newContext(ctxOptions);
        Page page = ctx.newPage();
        page.navigate(urlToFetch, new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));
        return page;
    }

    public static void closePage(Page page) {
        if (page == null) return;
        BrowserContext ctx = page.context();
        try {
            ctx.close();
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public static void shutdown() {
        try {
            BROWSER.close();
        } finally {
            PLAYWRIGHT.close();
        }
    }
}