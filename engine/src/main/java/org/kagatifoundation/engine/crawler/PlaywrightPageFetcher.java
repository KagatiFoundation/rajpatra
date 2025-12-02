package org.kagatifoundation.engine.crawler;

import org.jspecify.annotations.NonNull;
import org.kagatifoundation.engine.browser.BrowserPool;
import org.kagatifoundation.engine.document.HtmlDocument;
import org.kagatifoundation.engine.document.PlaywrightPageToHtmlDocumentAdapter;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;

public class PlaywrightPageFetcher implements HtmlDocumentFetcher {
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
                System.err.println("Couldn't close PageSession");
            } finally {
                PlaywrightPageFetcher.releaseWrapper(wrapper);
            }
        }
    }

    /*
    Standard browser pool size for Rajpatra.
    */
    public static final int BROWSER_POOL_SIZE = 4;

    private static BrowserPool browserPool;

    private static void initBrowserPoolService() {
        browserPool = new BrowserPool(BROWSER_POOL_SIZE);
    }

    @Override
    public HtmlDocument fetch(String link) throws Exception {

        try (var pageSession = fetchPage(link)) {
            if (pageSession == null || pageSession.page() == null) {
                throw new Exception();
            }
            var htmlDocument = PlaywrightPageToHtmlDocumentAdapter.toHtmlDocument(pageSession.page());
            return htmlDocument;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static PageSession fetchPage(@NonNull String urlToFetch) throws Exception {
        if (browserPool == null) {
            initBrowserPoolService();
        }

        BrowserPool.BrowserWrapper browserWrapper = browserPool.acquire();
        Browser.NewContextOptions ctxOptions = new Browser.NewContextOptions()
            .setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36")
            .setViewportSize(1280, 800)
            .setIgnoreHTTPSErrors(true);

        BrowserContext ctx = browserWrapper.browser().newContext(ctxOptions);
        Page page = ctx.newPage();
        page.navigate(urlToFetch, new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));
        
        // new page session starts here
        return new PlaywrightPageFetcher.PageSession(browserWrapper, ctx, page);
    }

    public static void releaseWrapper(BrowserPool.BrowserWrapper wrapper) {
        if (browserPool != null && wrapper != null) {
            browserPool.release(wrapper);
        }
    }

    public static void shutdown() {
        if (browserPool != null) {
            browserPool.shutdown();
        }
    }
}