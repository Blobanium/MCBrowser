package io.github.blobanium.mcbrowser;

import com.cinemamod.mcef.MCEF;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.blobanium.mcbrowser.config.BrowserAutoConfig;
import io.github.blobanium.mcbrowser.feature.BrowserUtil;
import io.github.blobanium.mcbrowser.screen.BrowserScreen;
import io.github.blobanium.mcbrowser.util.BrowserImpl;
import io.github.blobanium.mcbrowser.util.BrowserScreenHelper;
import io.github.blobanium.mcbrowser.util.TabHolder;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class MCBrowser implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("MCBrowser");

    public static List<TabHolder> tabs = new ArrayList<>();
    public static List<String> closedTabs = new ArrayList<>();
    public static int activeTab = 0;
    private static boolean firstOpen = true;

    @Override
    public void onInitializeClient() {
        AutoConfig.register(BrowserAutoConfig.class, GsonConfigSerializer::new);

        if (MCEF.getSettings().getUserAgent().equals("null")) {
            MCEF.getSettings().setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) MCEF/2 Chrome/119.0.0.0 Safari/537.36");
        }

        for (String command : new String[]{"browser", "br"}) {
            ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> {
                dispatcher.register(ClientCommandManager.literal(command)
                        .executes(context -> {
                            openBrowser();
                            return 0;
                        }).then(ClientCommandManager.literal("url")
                                .then(ClientCommandManager.argument("url", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            openNewTab(BrowserUtil.prediffyURL(StringArgumentType.getString(context, "url")));
                                            return 0;
                                        }))
                        ).then(ClientCommandManager.literal("reset")
                                .executes(context -> {
                                    reset();
                                    return 0;
                                })
                        ).then(ClientCommandManager.literal("test")
                                .then(ClientCommandManager.argument("tabNumber", IntegerArgumentType.integer(0, 9))
                                        .executes(context -> {
                                            setActiveTab(IntegerArgumentType.getInteger(context, "tabNumber"));
                                            openBrowser();
                                            return 0;
                                        }))
                        )); //todo remove debug commands
            }));
        }
        runCompatabilityCheck();
    }

    private static final MinecraftClient minecraft = MinecraftClient.getInstance();

    public static void setActiveTab(int index) {
        activeTab = index;
        if (minecraft.currentScreen instanceof BrowserScreen) {
            BrowserScreenHelper.instance.updateWidgets();
        }
    }

    public static void openNewTab() {
        openNewTab(BrowserUtil.prediffyURL(getConfig().homePage));
    }

    public static void openNewTab(String url) {
        openNewTab(url, tabs.size());
    }

    public static void openNewTab(String url, int index) {
        tabs.add(index, new TabHolder(url));
        setActiveTab(index);
        if (minecraft.currentScreen instanceof BrowserScreen) {
            BrowserScreenHelper.instance.addTab(index);
        } else {
            openBrowser();
        }
    }

    public static void closeTab(int index) {
        if (BrowserScreenHelper.instance != null) {
            BrowserScreenHelper.instance.removeTab(index);
        }
        closedTabs.add(tabs.get(index).isInit() ? tabs.get(index).getBrowser().getURL() : tabs.get(index).holderUrl);
        tabs.get(index).close();
        tabs.remove(index);
        if (tabs.size() == 0 && BrowserScreenHelper.instance != null) {
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
        openNewTab(tabs.get(index).getBrowser().getURL(), index + 1);
        setActiveTab(index);
    }

    public static void openBrowser() {
        if (firstOpen) {
            firstOpen = false;
            loadTabsFromJson();
            setActiveTab(Math.max(0, tabs.size() - 1));
        }
        if (tabs.isEmpty()) {
            tabs.add(new TabHolder(BrowserUtil.prediffyURL(getConfig().homePage)));
        }
        minecraft.send(() -> minecraft.setScreen(new BrowserScreen(Text.literal("Basic Browser"))));
    }

    public static void reset() {
        for (TabHolder tab : tabs) {
            tab.close();
        }
        tabs.clear();
        activeTab = 0;
    }

    public static TabHolder getCurrentTabHolder() {
        return tabs.get(activeTab);
    }

    public static BrowserImpl getCurrentTab() {
        return tabs.get(activeTab).getBrowser();
    }

    public static String getCurrentUrl() {
        return getCurrentTab().getURL();
    }

    public static void saveTabsToJson() {
        ArrayList<String> urls = new ArrayList<>();
        for (TabHolder tab : tabs) {
            urls.add(tab.getBrowser().getURL());
        }
        try {
            FileWriter fileWriter = new FileWriter(FabricLoader.getInstance().getConfigDir().resolve("MCBrowser") + "\\tabs" + ".json");
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(urls, fileWriter);
            fileWriter.close();
        } catch (IOException e) {
            LOGGER.error("Could not save opened tabs for MCBrowser");
            e.printStackTrace();
            return;
        }
        LOGGER.info("Successfully saved tabs for MCBrowser");
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
                LOGGER.error("Could not read list of tabs from \"" + filename + "\"");
                e.printStackTrace();
            }
        }
    }

    public static BrowserAutoConfig getConfig() {
        return AutoConfig.getConfigHolder(BrowserAutoConfig.class).getConfig();
    }

    private static void runCompatabilityCheck(){
        String mcefVersion = FabricLoader.getInstance().getModContainer("mcef").get().getMetadata().getVersion().getFriendlyString();

        if(mcefVersion.contains("2.1.0")){
            LOGGER.warn("You are using a version of MCEF that is known to have critical bugs" +
                    "\nIt is strongly recomended to update MCEF to the latest version" +
                    "\n - You will be unable to sign into google while using this version. (See: https://github.com/CinemaMod/mcef/issues/61)" +
                    "\n - JCEF Processes created by MCEF may linger in the background and eat up your CPU Resources. Even after minecraft is shut down. (See: https://github.com/CinemaMod/mcef/issues/63)");
        }
    }
}
