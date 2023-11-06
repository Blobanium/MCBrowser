package io.github.blobanium.mcbrowser.mixin;

import io.github.blobanium.mcbrowser.util.BrowserScreenHelper;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(CefClient.class)
public class CefClientMixin {
    @Inject(at = @At("HEAD"), method = "onAddressChange", remap = false)
    public void onAddressChange(CefBrowser browser, CefFrame frame, String url, CallbackInfo ci){
        BrowserScreenHelper.currentUrl = url;
        if(url != null) {
            BrowserScreenHelper.instance.onUrlChange();
        }
    }


}
