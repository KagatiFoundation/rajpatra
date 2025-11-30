package org.kagatifoundation.engine.browser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;

public class BrowserPool {
    private final List<Browser> browsers = new ArrayList<>();
    private final Semaphore semaphore;

    public BrowserPool(int poolSize) {
        Playwright playwright = Playwright.create();
        semaphore = new Semaphore(poolSize);

        for (int i = 0; i < poolSize; i++) {
            Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                    .setHeadless(true)
                    .setArgs(List.of("--no-sandbox", "--disable-dev-shm-usage"))
            );
            browsers.add(browser);
        }
    }    

    public Browser acquire() throws InterruptedException {
        semaphore.acquire();
        synchronized (browsers) {
            return browsers.removeFirst();
        }
    }

    public void release(Browser browser) {
        synchronized (browsers) {
            browsers.add(browser);
        }
        semaphore.release();
    }

    public void shutdown() {
        for (Browser b: browsers) {
            b.close();
        }
    }
}