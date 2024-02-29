package io.github.blobanium.mcbrowser.mixin;

import io.github.blobanium.mcbrowser.util.BrowserCaches;
import io.github.blobanium.mcbrowser.util.BrowserImpl;
import io.github.blobanium.mcbrowser.util.BrowserUtil;
import io.github.blobanium.mcbrowser.util.TabManager;
import io.github.blobanium.mcbrowser.util.button.BrowserTabIcon;
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
        if (url != null && !(browser instanceof BrowserTabIcon) && (browser instanceof BrowserImpl)) {
            BrowserUtil.onUrlChange();
        }
        BrowserCaches.urlCache.put(browser.getIdentifier(), url);
    }

    @Inject(at = @At("HEAD"), method = "onTooltip", remap = false)
    public void onTooltip(CefBrowser browser, String text, CallbackInfoReturnable<Boolean> cir) {
        BrowserUtil.tooltipText = text;
    }

    @Inject(at = @At("HEAD"), method = "onLoadingStateChange", remap = false)
    public void onLoadingStateChange(CefBrowser browser, boolean isLoading, boolean canGoBack, boolean canGoForward, CallbackInfo ci) {
        BrowserUtil.instance.updateWidgets();
        BrowserCaches.isLoadingCache.put(browser.getIdentifier(), isLoading);
    }

    @Inject(at = @At("HEAD"), method = "onTitleChange", remap = false)
    public void onTitleChange(CefBrowser browser, String title, CallbackInfo ci) {
        TabManager.setTitleForTab(browser.getIdentifier(), title);
    }

}
