package io.github.blobanium.mcbrowser.util;

import com.cinemamod.mcef.MCEF;
import io.github.blobanium.mcbrowser.MCBrowser;
import io.github.blobanium.mcbrowser.feature.BrowserFeatureUtil;
import io.github.blobanium.mcbrowser.feature.specialbutton.SpecialButtonActions;
import io.github.blobanium.mcbrowser.screen.BrowserScreen;
import io.github.blobanium.mcbrowser.util.button.BrowserTabIcon;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

public class BrowserUtil {
    //Mouse position
    public static double lastMouseX;
    public static double lastMouseY;

    public static BrowserScreen instance;

    public static String tooltipText;

    //Navigation initialization methods
    public static ButtonWidget initButton(Text message, ButtonWidget.PressAction onPress, int positionX, int offset) {
        return ButtonWidget.builder(message, onPress)
                .dimensions(positionX, offset - 20, 15, 15)
                .build();
    }

    //Matrix related commands

    public static int mouseX(double x, int offset) {
        lastMouseX = x;
        return (int) ((x - offset) * MinecraftClient.getInstance().getWindow().getScaleFactor());
    }

    public static int mouseY(double y, int offset) {
        lastMouseY = y;
        return (int) ((y - offset) * MinecraftClient.getInstance().getWindow().getScaleFactor());
    }

    public static void updateMouseLocation(double mouseX, double mouseY) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;
    }

    public static int scaleX(double x, int offset) {
        return (int) ((x - offset * 2) * MinecraftClient.getInstance().getWindow().getScaleFactor());
    }

    public static int scaleY(double y, int offset) {
        return (int) ((y - offset * 2) * MinecraftClient.getInstance().getWindow().getScaleFactor());
    }

    public static int getUrlBoxWidth(int width, int offset) {
        return width - (offset * 2) - 80;
    }

    //Browser Creation
    public static BrowserImpl createBrowser(String url) {
        if (MCEF.isInitialized()) {
            BrowserImpl browser = new BrowserImpl(MCEF.getClient(), url, false);
            browser.setCloseAllowed();
            browser.createImmediately();
            return browser;
        } else {
            throw new IllegalStateException("Chromium Embedded Framework was never initialized.");
        }
    }

    public static BrowserTabIcon createIcon(String url) {
        if (MCEF.isInitialized()) {
            BrowserTabIcon icon = new BrowserTabIcon(MCEF.getClient(), url, false);
            icon.setCloseAllowed();
            icon.createImmediately();
            return icon;
        } else {
            throw new IllegalStateException("Chromium Embedded Framework was never initialized.");
        }
    }

    public static TextFieldWidget initUrlBox(int offset, int width) {
        TextFieldWidget urlBox = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, offset + 80, offset - 20, BrowserUtil.getUrlBoxWidth(width, offset), 15, Text.of("")) {
            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                if (isFocused()) {
                    for (TabHolder tab : TabManager.tabs) {
                        BrowserImpl browser = tab.getBrowser();
                        if (browser != null) {
                            tab.getBrowser().setFocus(false);
                        }
                    }
                    if (keyCode == GLFW.GLFW_KEY_ENTER) {
                        TabManager.getCurrentTab().loadURL(BrowserFeatureUtil.prediffyURL(getText()));
                        setFocused(false);
                    }
                }
                return super.keyPressed(keyCode, scanCode, modifiers);
            }

        };
        urlBox.setMaxLength(2048); //Most browsers have a max length of 2048
        return urlBox;
    }

    //Button related Methods
    public static void openInBrowser(){
        try {
            Util.getOperatingSystem().open(new URI(TabManager.getCurrentUrl()));
        } catch (URISyntaxException e) {
            MCBrowser.LOGGER.fatal("Unable to open Browser", e);
        }
    }

    public static void homeButtonAction(){
        String prediffyedHomePage = BrowserFeatureUtil.prediffyURL(MCBrowser.getConfig().homePage);
        instance.urlBox.setText(prediffyedHomePage);
        TabManager.getCurrentTab().loadURL(prediffyedHomePage);
    }

    //Event Methods
    public static void onUrlChange() {
        if (instance.urlBox.isFocused()) {
            instance.urlBox.setFocused(false);
        }
        instance.urlBox.setText(TabManager.getCurrentUrl());
        instance.urlBox.setCursorToStart(false);
        SpecialButtonActions action = SpecialButtonActions.getFromUrlConstantValue(TabManager.getCurrentUrl());
        if (action != null) {
            instance.specialButton.setMessage(action.getButtonText());
        }
    }

    public static class Keybinds{
        public static final int CTRL_T = GLFW.GLFW_MOD_CONTROL + GLFW.GLFW_KEY_T;
        public static final int CTRL_SHIFT_T = GLFW.GLFW_MOD_CONTROL + GLFW.GLFW_MOD_SHIFT + GLFW.GLFW_KEY_T;
        public static final int CTRL_TAB = GLFW.GLFW_MOD_CONTROL + GLFW.GLFW_KEY_TAB;
        public static final int CTRL_SHIFT_TAB = GLFW.GLFW_MOD_CONTROL + GLFW.GLFW_MOD_SHIFT + GLFW.GLFW_KEY_TAB;
    }

    public static void runAsyncIfEnabled(Runnable runnable){
        if (MCBrowser.getConfig().asyncBrowserInput) {
            CompletableFuture.runAsync(runnable);
        } else {
            runnable.run();
        }
    }
}
