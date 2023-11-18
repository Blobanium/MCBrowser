package io.github.blobanium.mcbrowser.mixin;

import io.github.blobanium.mcbrowser.util.BrowserImpl;
import io.github.blobanium.mcbrowser.util.BrowserScreenHelper;
import io.github.blobanium.mcbrowser.util.button.BrowserTabIcon;
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
    public void onAddressChange(CefBrowser browser, CefFrame frame, String url, CallbackInfo ci) {
        if (url != null) {
            if (!(browser instanceof BrowserTabIcon)) {
                if (browser instanceof BrowserImpl) {
                    BrowserScreenHelper.instance.onUrlChange();
                }
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "onTooltip", remap = false)
    public void onTooltip(CefBrowser browser, String text, CallbackInfoReturnable<Boolean> cir) {
        BrowserScreenHelper.tooltipText = text;
    }

    @Inject(at = @At("HEAD"), method = "onLoadingStateChange", remap = false)
    public void onLoadingStateChange(CefBrowser browser, boolean isLoading, boolean canGoBack, boolean canGoForward, CallbackInfo ci) {
        BrowserScreenHelper.instance.reloadButton.setMessage(Text.of(isLoading ? "\u274C" : "\u27F3"));
        BrowserScreenHelper.instance.forwardButton.active = canGoForward;
        BrowserScreenHelper.instance.backButton.active = canGoBack;
    }

}
