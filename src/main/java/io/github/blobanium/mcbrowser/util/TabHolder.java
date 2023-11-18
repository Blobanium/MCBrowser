package io.github.blobanium.mcbrowser.util;

import io.github.blobanium.mcbrowser.util.button.BrowserTabIcon;

public class TabHolder {
    public String holderUrl;
    BrowserImpl browser;
    BrowserTabIcon icon = null;
    private boolean init = false;

    public TabHolder(String url) {
        holderUrl = url;
        browser = BrowserScreenHelper.createBrowser("");
    }

    public void init() {
        if (browser.isLoading()) {
            browser.stopLoad();
        }
        browser.loadURL(holderUrl);
        init = true;
    }

    public boolean isInit() {
        return init;
    }

    public void initIcon(String url) {
        icon = BrowserScreenHelper.createIcon(url);
    }

    public BrowserImpl getBrowser() {
        return browser;
    }

    public BrowserTabIcon getIcon() {
        return icon;
    }

    public void resetIcon() {
        if (icon == null) {
            return;
        }
        icon.close();
        icon = null;
    }

    public void close() {
        if (isInit()) {
            browser.close();
            resetIcon();
        }
    }
}
