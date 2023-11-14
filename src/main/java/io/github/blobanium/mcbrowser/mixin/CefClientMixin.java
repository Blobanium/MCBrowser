package io.github.blobanium.mcbrowser.mixin;

import io.github.blobanium.mcbrowser.util.BrowserScreenHelper;
import net.minecraft.text.Text;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(CefClient.class)
public class CefClientMixin {
    @Inject(at = @At("HEAD"), method = "onAddressChange", remap = false)
    public void onAddressChange(CefBrowser browser, CefFrame frame, String url, CallbackInfo ci){
        BrowserScreenHelper.currentUrl = url;
        if(url != null) {
            BrowserScreenHelper.onUrlChange();
        }
    }

    @Inject(at = @At("HEAD"), method = "onTooltip", remap = false)
    public void onTooltip(CefBrowser browser, String text, CallbackInfoReturnable<Boolean> cir){
        BrowserScreenHelper.tooltipText = text;
    }

    @Inject(at = @At("HEAD"), method = "onLoadingStateChange", remap = false)
    public void onLoadingStateChange(CefBrowser browser, boolean isLoading, boolean canGoBack, boolean canGoForward, CallbackInfo ci){
        if(isLoading){
            BrowserScreenHelper.instance.reloadButton.setMessage(Text.of("\u274C"));
        } else {
            BrowserScreenHelper.instance.reloadButton.setMessage(Text.of("\u27F3"));
        }

        BrowserScreenHelper.instance.forwardButton.active = canGoForward;
        BrowserScreenHelper.instance.backButton.active = canGoBack;
    }

}
