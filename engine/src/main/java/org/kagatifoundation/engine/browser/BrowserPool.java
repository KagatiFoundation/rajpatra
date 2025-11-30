package org.kagatifoundation.engine.browser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;

public class BrowserPool {
    public static record BrowserWrapper(Playwright playwright, Browser browser) {}

    private final List<BrowserWrapper> pool = new ArrayList<>();
    private final Semaphore semaphore;

    public BrowserPool(int poolSize) {
        semaphore = new Semaphore(poolSize);

        for (int i = 0; i < poolSize; i++) {
            Playwright pw = Playwright.create();
            Browser browser = pw.chromium().launch(
                    new BrowserType.LaunchOptions()
                            .setHeadless(true)
                            .setArgs(List.of("--no-sandbox", "--disable-dev-shm-usage"))
            );
            pool.add(new BrowserWrapper(pw, browser));
        }
    }

    public BrowserWrapper acquire() throws InterruptedException {
        semaphore.acquire();
        synchronized (pool) {
            return pool.remove(0);
        }
    }

    public void release(BrowserWrapper wrapper) {
        synchronized (pool) {
            pool.add(wrapper);
        }
        semaphore.release();
    }

    public void shutdown() {
        synchronized (pool) {
            for (BrowserWrapper wrapper : pool) {
                try {
                    wrapper.browser.close();
                } catch (Exception e) {
                    System.err.println("Failed to close browser: " + e.getMessage());
                }
                try {
                    wrapper.playwright.close();
                } catch (Exception e) {
                    System.err.println("Failed to close Playwright: " + e.getMessage());
                }
            }
            pool.clear();
        }
    }
}