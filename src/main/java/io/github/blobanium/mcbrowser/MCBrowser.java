package io.github.blobanium.mcbrowser;

import io.github.blobanium.mcbrowser.screen.BrowserScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;


public class MCBrowser implements ClientModInitializer {
    private static boolean requestOpen = false;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.START_CLIENT_TICK.register((client) -> onTick());

        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("browser")
                    .executes(context -> {
                        requestOpen = true;
                        return 0;
                    })
            );
        }));
    }
    private static final MinecraftClient minecraft = MinecraftClient.getInstance();

    // H key to open a BasicBrowser screen
    public static final KeyBinding KEY_MAPPING = new KeyBinding(
            "Open Basic Browser", InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_H, "key.categories.misc"
    );

    public void onTick() {
        // Check if our key was pressed
        if ((KEY_MAPPING.wasPressed() || requestOpen) && !(minecraft.currentScreen instanceof BrowserScreen)) {
            //Display the web browser UI.
            minecraft.setScreen(new BrowserScreen(
                    Text.literal("Basic Browser")
            ));
        }
    }
}
