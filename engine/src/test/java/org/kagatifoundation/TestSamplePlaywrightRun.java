package org.kagatifoundation;

import org.junit.Test;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Playwright;

public class TestSamplePlaywrightRun {
    @Test
    public void testSamplePlaywrightRun() {
        Playwright pw = Playwright.create();
        Browser browser = pw.chromium().launch();
        System.out.println("It works!");
        browser.close();
        pw.close();
    }    
}