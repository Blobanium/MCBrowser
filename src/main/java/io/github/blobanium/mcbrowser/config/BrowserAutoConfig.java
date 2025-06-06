package io.github.blobanium.mcbrowser.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "mcbrowser")
public class BrowserAutoConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip
    public boolean openLinkInBrowser = true;
    @ConfigEntry.Gui.Tooltip
    public String homePage = "https://www.google.com";

    @ConfigEntry.Category("performance")
    @ConfigEntry.Gui.Tooltip
    public boolean asyncBrowserInput = true;

    @ConfigEntry.Gui.Tooltip
    public boolean saveTabs = true;

    @ConfigEntry.Category("performance")
    @ConfigEntry.Gui.Tooltip
    public boolean limitBrowserFramerate = false;

    @ConfigEntry.Category("performance")
    @ConfigEntry.Gui.Tooltip
    public int browserFPS = 60;

    @ConfigEntry.Gui.Tooltip
    public double zoomScalingFactor = 0.5;

    @ConfigEntry.Category("privacyandsafety")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.RequiresRestart
    public boolean saveCookies = false;

    @ConfigEntry.Category("privacyandsafety")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.RequiresRestart
    public boolean enableMediaStream = false;

    @ConfigEntry.Category("privacyandsafety")
    @ConfigEntry.Gui.Tooltip
    public boolean allowDownloads = false;

    @ConfigEntry.Gui.Tooltip
    public boolean killJcefHelperOnClose = true;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.RequiresRestart
    public String customSwitches = "";
}
