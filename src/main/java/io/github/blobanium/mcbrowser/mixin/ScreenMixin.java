package io.github.blobanium.mcbrowser.mixin;

import io.github.blobanium.mcbrowser.MCBrowser;

import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.MalformedURLException;
import java.net.URI;

@Mixin(Screen.class)
public class ScreenMixin {
    @Inject(at = @At("HEAD"), method = "openLink", cancellable = true)
    private void openLink(URI link, CallbackInfo ci){

            try {
                if(MCBrowser.getConfig().openLinkInBrowser) {
                    MCBrowser.openBrowser(link.toURL().toString());
                    ci.cancel();
                }
            } catch (MalformedURLException e) {
                System.err.println("Opening in browser. Failed to convert to URL");
                e.printStackTrace();
            }
    }
}
