package io.github.blobanium.mcbrowser;

import com.cinemamod.mcef.MCEF;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.blobanium.mcbrowser.config.BrowserAutoConfig;
import io.github.blobanium.mcbrowser.feature.BrowserFeatureUtil;
import io.github.blobanium.mcbrowser.screen.BrowserScreen;
import io.github.blobanium.mcbrowser.util.TabHolder;
import io.github.blobanium.mcbrowser.util.TabManager;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class MCBrowser implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("MCBrowser");

    private static boolean firstOpen = true;
    public static boolean isShuttingDown = false;

    @Override
    public void onInitializeClient() {
        AutoConfig.register(BrowserAutoConfig.class, GsonConfigSerializer::new);

        if (MCEF.getSettings().getUserAgent().equals("null")) {
            MCEF.getSettings().setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) MCEF/2 Chrome/119.0.0.0 Safari/537.36");
        }

        for (String command : new String[]{"browser", "br"}) {
            ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal(command)
                    .executes(context -> {
                        openBrowser();
                        return 0;
                    }).then(ClientCommandManager.literal("url")
                            .then(ClientCommandManager.argument("url", StringArgumentType.greedyString())
                                    .executes(context -> {
                                        TabManager.openNewTab(BrowserFeatureUtil.prediffyURL(StringArgumentType.getString(context, "url")));
                                        return 0;
                                    }))
                    ).then(ClientCommandManager.literal("close")
                            .executes(context -> {
                                TabManager.reset();
                                return 0;
                            })
                    ))));
        }

        ClientCommandRegistrationCallback.EVENT.register((commandDispatcher, commandRegistryAccess) -> commandDispatcher.register(ClientCommandManager.literal("wiki")
                .executes(commandContext -> {
                    TabManager.openNewTab(BrowserFeatureUtil.prediffyURL("https://minecraft.wiki/"));
                    return 0;
                })));
    }

    private static final MinecraftClient minecraft = MinecraftClient.getInstance();

    public static void openBrowser() {
        if (firstOpen) {
            TabManager.loadTabsFromJson();
            TabManager.setActiveTab(Math.max(0, TabManager.tabs.size() - 1));
        }
        firstOpen = false;
        if (TabManager.tabs.isEmpty()) {
            TabManager.tabs.add(new TabHolder(BrowserFeatureUtil.prediffyURL(getConfig().homePage)));
        }
        minecraft.send(() -> minecraft.setScreen(new BrowserScreen(Text.literal("Basic Browser"))));
    }

    public static BrowserAutoConfig getConfig() {
        return AutoConfig.getConfigHolder(BrowserAutoConfig.class).getConfig();
    }

    public static void sendToastMessage(Text title, Text description) {
        MinecraftClient.getInstance().getToastManager().add(new SystemToast(new SystemToast.Type(), title, description));
    }
}
