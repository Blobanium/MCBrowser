package io.github.blobanium.mcbrowser.mixin;

import com.cinemamod.mcef.CefUtil;
import io.github.blobanium.mcbrowser.MCBrowser;
import net.fabricmc.loader.api.FabricLoader;
import org.cef.CefSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(CefUtil.class)
public class CefUtilMixin {
    @ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Ljava/util/Objects;requireNonNull(Ljava/lang/Object;)Ljava/lang/Object;"), remap = false, index = 0)
    private static Object injected(Object obj) {
        if (MCBrowser.getConfig().saveCookies) {
            ((CefSettings) obj).cache_path = FabricLoader.getInstance().getConfigDir().resolve("MCBrowser") + "/browser";
            ((CefSettings) obj).persist_session_cookies = true;
        }
        return obj;
    }
}
