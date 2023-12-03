package io.github.blobanium.mcbrowser.util;

import com.cinemamod.mcef.MCEF;
import io.github.blobanium.mcbrowser.MCBrowser;
import io.github.blobanium.mcbrowser.feature.BrowserUtil;
import io.github.blobanium.mcbrowser.feature.specialbutton.SpecialButtonActions;
import io.github.blobanium.mcbrowser.screen.BrowserScreen;
import io.github.blobanium.mcbrowser.util.button.BrowserTabIcon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;
import java.net.MalformedURLException;
import java.net.URL;

import static io.github.blobanium.mcbrowser.MCBrowser.*;

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

    public static TextFieldWidget initUrlBox(int offset, int width) {
        TextFieldWidget urlBox = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, offset + 80, offset - 20, BrowserScreenHelper.getUrlBoxWidth(width, offset), 15, Text.of("")) {
            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                if (isFocused()) {
                    for (TabHolder tab : tabs) {
                        BrowserImpl browser = tab.getBrowser();
                        if (browser != null) {
                            tab.getBrowser().setFocus(false);
                        }
                    }
                    if (keyCode == GLFW.GLFW_KEY_ENTER) {
                        getCurrentTab().loadURL(BrowserUtil.prediffyURL(getText()));
                        setFocused(false);
                    }
                }
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
        };
        if (instance.initURL != null) {
            urlBox.setText(instance.initURL);
        }
        urlBox.setMaxLength(2048); //Most browsers have a max length of 2048
        return urlBox;
    }

    //Button related Methods
    public static void openInBrowser(){
        try {
            Util.getOperatingSystem().open(new URL(getCurrentUrl()));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void homeButtonAction(){
        String prediffyedHomePage = BrowserUtil.prediffyURL(MCBrowser.getConfig().homePage);
        instance.urlBox.setText(prediffyedHomePage);
        getCurrentTab().loadURL(prediffyedHomePage);
    }

    //Event Methods
    public static void onUrlChange() {
        if (instance.urlBox.isFocused()) {
            instance.urlBox.setFocused(false);
        }
        instance.urlBox.setText(getCurrentUrl());
        instance.urlBox.setCursorToStart();
        SpecialButtonActions action = SpecialButtonActions.getFromUrlConstantValue(getCurrentUrl());
        if (action != null) {
            instance.specialButton.setMessage(action.getButtonText());
        }
    }
}
