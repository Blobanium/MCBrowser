package io.github.blobanium.mcbrowser;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;


public class MCBrowser implements ModInitializer {
    @Override
    public void onInitialize() {
        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("browser")
                    .executes(context -> {
                        return 0;
                    })
            );
        }));
    }
}
