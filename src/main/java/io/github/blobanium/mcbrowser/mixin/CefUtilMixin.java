package io.github.blobanium.mcbrowser.mixin;

import io.github.blobanium.mcbrowser.MCBrowser;
import net.fabricmc.loader.api.FabricLoader;
import org.cef.CefSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.ArrayList;
import java.util.List;

@Mixin(targets = "com.cinemamod.mcef.CefUtil")
public class CefUtilMixin {
    @ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Lorg/cef/CefApp;getInstance([Ljava/lang/String;Lorg/cef/CefSettings;)Lorg/cef/CefApp;"), remap = false, index = 0)
    private static String[] customCefSwitches(String[] args) {
        if (MCBrowser.getConfig().enableMediaStream) {
            ArrayList<String> list = new ArrayList<>(List.of(args));
            list.add("--enable-media-stream");
            args = list.toArray(new String[0]);
        }
        return args;
    }

    @ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Lorg/cef/CefApp;getInstance([Ljava/lang/String;Lorg/cef/CefSettings;)Lorg/cef/CefApp;"), remap = false, index = 1)
    private static CefSettings customCefSettings(CefSettings settings) {
        if (MCBrowser.getConfig().saveCookies) {
            settings.cache_path = FabricLoader.getInstance().getConfigDir().resolve("MCBrowser") + "/browser";
            settings.persist_session_cookies = true;
        }
        return settings;
    }
}
