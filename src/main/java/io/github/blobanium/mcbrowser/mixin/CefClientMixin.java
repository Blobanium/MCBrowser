package io.github.blobanium.mcbrowser.mixin;

import io.github.blobanium.mcbrowser.MCBrowser;
import io.github.blobanium.mcbrowser.util.BrowserCaches;
import io.github.blobanium.mcbrowser.util.BrowserImpl;
import io.github.blobanium.mcbrowser.util.BrowserUtil;
import io.github.blobanium.mcbrowser.util.TabManager;
import io.github.blobanium.mcbrowser.util.button.BrowserTabIcon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefBeforeDownloadCallback;
import org.cef.callback.CefDownloadItem;
import org.cef.callback.CefDownloadItemCallback;
import org.cef.callback.CefFileDialogCallback;
import org.cef.handler.CefDialogHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Vector;

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

    @Inject(at = @At("HEAD"), method = "onBeforeDownload", remap = false)
    public void onBeforeDownload(CefBrowser browser, CefDownloadItem downloadItem, String suggestedName, CefBeforeDownloadCallback callback, CallbackInfo ci){
        if(MCBrowser.getConfig().allowDownloads){
            //TODO: add download code.
            System.out.println("suggestedname=" + suggestedName);
            MCBrowser.sendToastMessage(Text.translatable("mcbrowser.download.toast.started"), Text.translatable("mcbrowser.download.toast.started.description"));
            callback.Continue(suggestedName, true);
        }else{
            MCBrowser.sendToastMessage(Text.translatable("mcbrowser.download.toast.disabled"), Text.translatable("mcbrowser.download.toast.disabled.description"));
        }
    }

    @Inject(at = @At("HEAD"), method = "onDownloadUpdated", remap = false)
    public void onDownloadUpdated(CefBrowser browser, CefDownloadItem downloadItem, CefDownloadItemCallback callback, CallbackInfo ci){
        if(MCBrowser.isShuttingDown){
            callback.cancel();
        }
        System.out.println("Downloading " + downloadItem.getSuggestedFileName() + " (" + downloadItem.getPercentComplete() + "% Complete  (" + downloadItem.getCurrentSpeed() + " bytes/s))");
        if(downloadItem.isComplete()){
            MCBrowser.sendToastMessage(Text.translatable("mcbrowser.download.toast.complete"), Text.translatable("mcbrowser.download.toast.completed.description"));
        }
    }

}
