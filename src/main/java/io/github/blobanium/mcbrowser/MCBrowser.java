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
import net.fabricmc.loader.api.FabricLoader;
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

        runCompatabilityCheck();
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
