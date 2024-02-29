package io.github.blobanium.mcbrowser.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "mcbrowser")
public class BrowserAutoConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip
    public final boolean openLinkInBrowser = true;
    @ConfigEntry.Gui.Tooltip
    public final String homePage = "https://www.google.com";

    @ConfigEntry.Gui.Tooltip
    public final boolean asyncBrowserInput = true;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.RequiresRestart
    public final boolean saveCookies = false;

    @ConfigEntry.Gui.Tooltip
    public final boolean saveTabs = true;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.RequiresRestart
    public final boolean enableMediaStream = false;

    @ConfigEntry.Gui.Tooltip
    public final boolean limitBrowserFramerate=false;

    @ConfigEntry.Gui.Tooltip
    public final int browserFPS = 60;
}
