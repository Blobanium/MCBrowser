package io.github.blobanium.mcbrowser.util;

import io.github.blobanium.mcbrowser.util.button.BrowserTabIcon;

public class TabHolder {
    public final String holderUrl;
    BrowserImpl browser;
    BrowserTabIcon icon = null;
    private boolean init = false;
    public String holderTitle;

    public TabHolder(String url) {
        holderUrl = url;
    }

    public void init() {
        if (browser != null) {
            browser.close();
        }
        browser = BrowserUtil.createBrowser(holderUrl);
        init = true;
    }

    public boolean isInit() {
        return init;
    }

    public String getUrl() {
        return isInit() ? getBrowser().getURL() : holderUrl;
    }

    public void initIcon(String url) {
        String parsedUrl = url;
        if (url.contains("://")) {
            parsedUrl = url.substring(url.indexOf("://") + 3);
        }
        icon = BrowserUtil.createIcon(parsedUrl);
    }

    public BrowserImpl getBrowser() {
        return browser;
    }

    public BrowserTabIcon getIcon() {
        return icon;
    }

    public String getTitle() {
        return holderTitle;
    }

    public void setTitle(String title){
        holderTitle = title;
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
        }
        resetIcon();
    }
}
