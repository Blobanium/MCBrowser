package io.github.blobanium.mcbrowser;

import com.cinemamod.mcef.MCEF;
import io.github.blobanium.mcbrowser.screen.BrowserScreen;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;


public class MCBrowser implements ModInitializer {
    @Override
    public void onInitialize() {


        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("browser")
                    .executes(context -> {
                        MinecraftClient.getInstance().setScreen(new BrowserScreen(Text.literal("Browser")));
                        return 0;
                    })
            );
        }));
    }
}
