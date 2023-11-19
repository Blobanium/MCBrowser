package io.github.blobanium.mcbrowser.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "mcbrowser")
public class BrowserAutoConfig implements ConfigData {
    public boolean openLinkInBrowser = true;
    public String homePage = "https://www.google.com";

    public boolean asyncBrowserInput = true;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.RequiresRestart
    public boolean saveCookies = false;

    @ConfigEntry.Gui.Tooltip
    public boolean saveTabs = true;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.RequiresRestart
    public boolean enableMediaStream = false;
}
