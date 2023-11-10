package io.github.blobanium.mcbrowser;

import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.blobanium.mcbrowser.config.BrowserAutoConfig;
import io.github.blobanium.mcbrowser.feature.BrowserUtil;
import io.github.blobanium.mcbrowser.screen.BrowserScreen;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class MCBrowser implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("MCBrowser");

    public static boolean requestOpen = false;
    private static String url;

    @Override
    public void onInitializeClient() {
        AutoConfig.register(BrowserAutoConfig.class, GsonConfigSerializer::new);

        ClientTickEvents.START_CLIENT_TICK.register((client) -> onTick());
        for (String command : new String[]{"browser", "br"}) {
            ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> {
                dispatcher.register(ClientCommandManager.literal(command)
                        .executes(context -> {
                            openBrowser(BrowserUtil.prediffyURL(getConfig().homePage));
                            return 0;
                        }).then(ClientCommandManager.argument("url", StringArgumentType.greedyString())
                                .executes(context -> {
                                    openBrowser(BrowserUtil.prediffyURL(StringArgumentType.getString(context, "url")));
                                    return 0;
                                })
                        )
                );
            }));
        }
    }
    private static final MinecraftClient minecraft = MinecraftClient.getInstance();


    public void onTick() {
        // Check if our key was pressed
        if (requestOpen && !(minecraft.currentScreen instanceof BrowserScreen)) {
            //Display the web browser UI.

            minecraft.setScreen(new BrowserScreen(
                    Text.literal("Basic Browser"),
                    url
            ));
        }
    }

    public static void openBrowser(String targetUrl){
        url = targetUrl;
        requestOpen = true;
    }

    public static BrowserAutoConfig getConfig(){
        return AutoConfig.getConfigHolder(BrowserAutoConfig.class).getConfig();
    }
}
