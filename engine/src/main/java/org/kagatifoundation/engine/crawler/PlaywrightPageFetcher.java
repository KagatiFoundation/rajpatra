package org.kagatifoundation.engine.crawler;

import org.jspecify.annotations.NonNull;
import org.kagatifoundation.engine.browser.BrowserPool;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;

public class PlaywrightPageFetcher {
    /*
    Standard browser pool size for Rajpatra.
    */
    public static final int BROWSER_POOL_SIZE = 4;

    private static final BrowserPool browserPool = new BrowserPool(BROWSER_POOL_SIZE);

    public static Page fetchPage(@NonNull String urlToFetch) throws Exception {
        Browser browser = browserPool.acquire();
        Browser.NewContextOptions ctxOptions = new Browser.NewContextOptions()
            .setUserAgent(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36"
            )
            .setViewportSize(1280, 800)
            .setIgnoreHTTPSErrors(true);

        BrowserContext ctx = browser.newContext(ctxOptions);
        Page page = ctx.newPage();
        page.navigate(urlToFetch, new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));
        browserPool.release(browser);
        return page;
    }

    public static void closePage(Page page) {
        if (page == null) return;
        BrowserContext ctx = page.context();
        try {
            ctx.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void shutdown() {
        browserPool.shutdown();
    }
}