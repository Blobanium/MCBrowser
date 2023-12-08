package io.github.blobanium.mcbrowser;

import com.cinemamod.mcef.MCEF;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.blobanium.mcbrowser.config.BrowserAutoConfig;
import io.github.blobanium.mcbrowser.feature.BrowserUtil;
import io.github.blobanium.mcbrowser.screen.BrowserScreen;
import io.github.blobanium.mcbrowser.util.TabHolder;
import io.github.blobanium.mcbrowser.util.TabManager;
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


public class MCBrowser implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("MCBrowser");

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
                                            TabManager.openNewTab(BrowserUtil.prediffyURL(StringArgumentType.getString(context, "url")));
                                            return 0;
                                        }))
                        ).then(ClientCommandManager.literal("close")
                                .executes(context -> {
                                    TabManager.reset();
                                    return 0;
                                })
                        ));
            }));
        }
        runCompatabilityCheck();
    }

    private static final MinecraftClient minecraft = MinecraftClient.getInstance();

    public static void openBrowser() {
        if (firstOpen) {
            TabManager.loadTabsFromJson();
            TabManager.setActiveTab(Math.max(0, TabManager.tabs.size() - 1));
        }
        firstOpen = false;
        if (TabManager.tabs.isEmpty()) {
            TabManager.tabs.add(new TabHolder(BrowserUtil.prediffyURL(getConfig().homePage)));
        }
        minecraft.send(() -> minecraft.setScreen(new BrowserScreen(Text.literal("Basic Browser"))));
    }

    public static BrowserAutoConfig getConfig() {
        return AutoConfig.getConfigHolder(BrowserAutoConfig.class).getConfig();
    }

    private static void runCompatabilityCheck(){
        String mcefVersion = FabricLoader.getInstance().getModContainer("mcef").get().getMetadata().getVersion().getFriendlyString();

        if(mcefVersion.contains("2.1.0")){
            LOGGER.warn("""
                    You are using a version of MCEF that is known to have critical bugs
                    It is strongly recomended to update MCEF to the latest version
                     - You will be unable to sign into google while using this version. (See: https://github.com/CinemaMod/mcef/issues/61)
                     - JCEF Processes created by MCEF may linger in the background and eat up your CPU Resources. Even after minecraft is shut down. (See: https://github.com/CinemaMod/mcef/issues/63)""");
        }
    }
}
