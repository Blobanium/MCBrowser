package io.github.blobanium.mcbrowser.util;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.blobanium.mcbrowser.MCBrowser;
import io.github.blobanium.mcbrowser.feature.BrowserUtil;
import io.github.blobanium.mcbrowser.screen.BrowserScreen;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TabManager {
    public static List<TabHolder> tabs = new ArrayList<>();
    public static List<String> closedTabs = new ArrayList<>();
    public static int activeTab = 0;

    public static void setActiveTab(int index) {
        activeTab = index;
        if (MinecraftClient.getInstance().currentScreen instanceof BrowserScreen) {
            BrowserScreenHelper.instance.updateWidgets();
        }
    }

    public static void openNewTab() {
        openNewTab(BrowserUtil.prediffyURL(MCBrowser.getConfig().homePage));
    }

    public static void openNewTab(String url) {
        openNewTab(url, tabs.size());
    }

    public static void openNewTab(String url, int index) {
        openNewTab(url, index, index);
    }

    public static void openNewTab(String url, int index, int setActive) {
        tabs.add(index, new TabHolder(url));
        setActiveTab(setActive);
        if (MinecraftClient.getInstance().currentScreen instanceof BrowserScreen) {
            BrowserScreenHelper.instance.addTab(index);
        } else {
            MCBrowser.openBrowser();
        }
    }

    public static void closeTab(int index) {
        if (BrowserScreenHelper.instance != null) {
            BrowserScreenHelper.instance.removeTab(index);
        }
        closedTabs.add(tabs.get(index).getUrl());
        tabs.get(index).close();
        tabs.remove(index);
        if (tabs.isEmpty() && BrowserScreenHelper.instance != null) {
            BrowserScreenHelper.instance.close();
            return;
        }
        if (index <= activeTab && activeTab != 0) {
            setActiveTab(activeTab - 1);
        } else if (BrowserScreenHelper.instance != null) {
            BrowserScreenHelper.instance.updateWidgets();
        }
    }

    public static void copyTab(int index) {
        openNewTab(tabs.get(index).getUrl(), index + 1, index);
    }

    public static TabHolder getCurrentTabHolder() {
        return tabs.get(activeTab);
    }

    public static BrowserImpl getCurrentTab() {
        if (!tabs.get(activeTab).isInit()) {
            tabs.get(activeTab).init();
        }
        return tabs.get(activeTab).getBrowser();
    }

    public static void saveTabsToJson() {
        ArrayList<String> urls = new ArrayList<>();
        for (TabHolder tab : tabs) {
            urls.add(tab.getUrl());
        }
        try {
            FileWriter fileWriter = new FileWriter(FabricLoader.getInstance().getConfigDir().resolve("MCBrowser") + "\\tabs" + ".json");
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(urls, fileWriter);
            fileWriter.close();
        } catch (IOException e) {
            MCBrowser.LOGGER.error("Could not save opened tabs for MCBrowser");
            e.printStackTrace();
            return;
        }
        MCBrowser.LOGGER.info("Successfully saved tabs for MCBrowser");
    }

    public static void setTitleForTab(int identifier, String title){
        for(TabHolder tab: tabs){
            if(identifier == tab.getBrowser().getIdentifier()){
                tab.setTitle(title);
            }
        }
    }

    public static void loadTabsFromJson() {
        String filename = FabricLoader.getInstance().getConfigDir().resolve("MCBrowser") + "\\tabs" + ".json";
        if (new File(filename).exists()) {
            try {
                FileReader fileReader = new FileReader(filename);
                Type type = new TypeToken<ArrayList<String>>() {
                }.getType();
                Gson gson = new Gson();
                ArrayList<String> urls = gson.fromJson(fileReader, type);
                fileReader.close();
                for (String url : urls) {
                    if (!url.isEmpty()) {
                        TabHolder tab = new TabHolder(url);
                        tabs.add(tab);
                    }
                }
            } catch (IOException e) {
                MCBrowser.LOGGER.error("Could not read list of tabs from \"" + filename + "\"");
                e.printStackTrace();
            }
        }
    }

    public static void reset() {
        for (TabHolder tab : tabs) {
            tab.close();
        }
        tabs.clear();
        activeTab = 0;
    }

    public static String getCurrentUrl() {
        return getCurrentTab().getURL();
    }
}
