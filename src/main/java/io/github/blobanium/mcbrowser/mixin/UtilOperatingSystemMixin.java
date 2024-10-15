package io.github.blobanium.mcbrowser.mixin;

import io.github.blobanium.mcbrowser.MCBrowser;
import io.github.blobanium.mcbrowser.util.TabManager;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.MalformedURLException;
import java.net.URI;

@Mixin(Util.OperatingSystem.class)
public class UtilOperatingSystemMixin {
    @Inject(method = "open(Ljava/net/URI;)V", at = @At("HEAD"), cancellable = true)
    private void open(URI uri, CallbackInfo ci){
        try {
            if (MCBrowser.getConfig().openLinkInBrowser && (uri.getScheme().equals("http") || uri.getScheme().equals("https"))) {
                TabManager.openNewTab(uri.toURL().toString());
                ci.cancel();
            }
        } catch (MalformedURLException e) {
            MCBrowser.LOGGER.error("Opening in browser. Failed to convert to URL", e);
        }
    }
}
