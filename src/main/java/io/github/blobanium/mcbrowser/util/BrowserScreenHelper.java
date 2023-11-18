package io.github.blobanium.mcbrowser.util;

import com.cinemamod.mcef.MCEF;
import io.github.blobanium.mcbrowser.screen.BrowserScreen;
import io.github.blobanium.mcbrowser.util.button.BrowserTabIcon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class BrowserScreenHelper {
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
            throw new RuntimeException("Chromium Embedded Framework was never initialized.");
        }
    }

    public static BrowserTabIcon createIcon(String url) {
        if (MCEF.isInitialized()) {
            BrowserTabIcon icon = new BrowserTabIcon(MCEF.getClient(), url, false);
            icon.setCloseAllowed();
            icon.createImmediately();
            return icon;
        } else {
            throw new RuntimeException("Chromium Embedded Framework was never initialized.");
        }
    }
}
