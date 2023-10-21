package io.github.blobanium.mcbrowser;

import io.github.blobanium.mcbrowser.config.BrowserAutoConfig;
import io.github.blobanium.mcbrowser.screen.BrowserScreen;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;


public class MCBrowser implements ClientModInitializer {
    public static boolean requestOpen = false;
    private static String url = "https://www.google.com";

    public static BrowserAutoConfig config;

    @Override
    public void onInitializeClient() {
        AutoConfig.register(BrowserAutoConfig.class, GsonConfigSerializer::new);

        config = AutoConfig.getConfigHolder(BrowserAutoConfig.class).getConfig();

        ClientTickEvents.START_CLIENT_TICK.register((client) -> onTick());

        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("browser")
                    .executes(context -> {
                        openBrowser("https://www.google.com");
                        return 0;
                    })
            );
        }));

        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("br")
                    .executes(context -> {
                        openBrowser("https://www.google.com");
                        return 0;
                    })
            );
        }));
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
}
