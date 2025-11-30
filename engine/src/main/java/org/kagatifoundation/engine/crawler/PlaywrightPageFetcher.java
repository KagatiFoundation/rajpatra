package org.kagatifoundation.engine.crawler;

import org.jspecify.annotations.NonNull;
import org.kagatifoundation.engine.browser.BrowserPool;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;

public class PlaywrightPageFetcher {
    /**
     * Represents a page.
     */
    public static class PageSession implements AutoCloseable {
        private final BrowserPool.BrowserWrapper wrapper;
        private final BrowserContext context;
        private final Page page;

        public PageSession(BrowserPool.BrowserWrapper browserWrapper, BrowserContext context, Page page) {
            this.wrapper = browserWrapper;
            this.context = context;
            this.page = page;
        }

        public Page page() {
            return page;
        }

        @Override
        public void close() throws Exception {
            try {
                context.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                PlaywrightPageFetcher.releaseWrapper(wrapper);
            }
        }
    }

    /*
    Standard browser pool size for Rajpatra.
    */
    public static final int BROWSER_POOL_SIZE = 4;

    private static final BrowserPool browserPool = new BrowserPool(BROWSER_POOL_SIZE);

    public static PageSession fetchPage(@NonNull String urlToFetch) throws Exception {
        BrowserPool.BrowserWrapper browserWrapper = browserPool.acquire();
        Browser.NewContextOptions ctxOptions = new Browser.NewContextOptions()
            .setUserAgent(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36"
            )
            .setViewportSize(1280, 800)
            .setIgnoreHTTPSErrors(true);

        BrowserContext ctx = browserWrapper.browser().newContext(ctxOptions);
        Page page = ctx.newPage();
        page.navigate(urlToFetch, new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));
        
        // new page session starts here
        return new PlaywrightPageFetcher.PageSession(browserWrapper, ctx, page);
    }

    public static void releaseWrapper(BrowserPool.BrowserWrapper wrapper) {
        browserPool.release(wrapper);
    }

    public static void shutdown() {
        browserPool.shutdown();
    }
}