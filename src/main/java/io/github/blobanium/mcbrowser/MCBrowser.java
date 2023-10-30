package io.github.blobanium.mcbrowser;

import io.github.blobanium.mcbrowser.config.BrowserAutoConfig;
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

        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("browser")
                    .executes(context -> {
                        openBrowser(getConfig().homePage);
                        return 0;
                    })
            );
        }));

        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("br")
                    .executes(context -> {
                        openBrowser(getConfig().homePage);
                        return 0;
                    })
            );
        }));

        String mcefVersion = FabricLoader.getInstance().getModContainer("mcef").get().getMetadata().getVersion().getFriendlyString();

        //Check if using (Soon-to-be) outdated MCEF Version
        if(mcefVersion.contains("2.0.1")){
            LOGGER.warn("You are using a version of MCEF that will be no longer supported by MCBrowser." +
                    "\nThe version of MCEF you are using is using a chromium build that is vulnerable to CVE-2023-4863" +
                    "\nSupport for this version will become discontinued in future versions of MCBrowser" +
                    "\nIt is strongly recommended to update MCEF when newer versions become available.");
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
