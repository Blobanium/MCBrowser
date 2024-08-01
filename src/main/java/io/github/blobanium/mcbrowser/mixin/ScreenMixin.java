package io.github.blobanium.mcbrowser.mixin;

import io.github.blobanium.mcbrowser.MCBrowser;
import io.github.blobanium.mcbrowser.util.TabManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.net.MalformedURLException;
import java.net.URI;

@Mixin(Screen.class)
public class ScreenMixin {
    @Shadow
    @Nullable
    protected MinecraftClient client;

    @Redirect(method = "handleTextClick",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util$OperatingSystem;open(Ljava/net/URI;)V"))
    private void openLink(Util.OperatingSystem instance, URI uri) {
        try {
            if (MCBrowser.getConfig().openLinkInBrowser) {
                TabManager.openNewTab(uri.toURL().toString());
            }
        } catch (MalformedURLException e) {
            MCBrowser.LOGGER.error("Opening in browser. Failed to convert to URL", e);
        }
    }
}
