package io.github.blobanium.mcbrowser.util;

import com.cinemamod.mcef.MCEF;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.blobanium.mcbrowser.MCBrowser;
import io.github.blobanium.mcbrowser.feature.BrowserUtil;
import io.github.blobanium.mcbrowser.screen.BrowserScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.net.MalformedURLException;
import java.net.URL;

public class BrowserScreenHelper {
    private static final int Z_SHIFT = -1;

    public static String currentUrl = null;

    //Mouse position
    public static double lastMouseX;
    public static double lastMouseY;

    public static BrowserScreen instance;

    public static String tooltipText;


    //Rendering
    public static void renderBrowser(int offset, int width, int height, int textureID){
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        RenderSystem.setShaderTexture(0, textureID);
        Tessellator t = Tessellator.getInstance();
        BufferBuilder buffer = t.getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        buffer.vertex(offset, height - offset, Z_SHIFT).texture(0.0f, 1.0f).color(255, 255, 255, 255).next();
        buffer.vertex(width - offset, height - offset, Z_SHIFT).texture(1.0f, 1.0f).color(255, 255, 255, 255).next();
        buffer.vertex(width - offset, offset, Z_SHIFT).texture(1.0f, 0.0f).color(255, 255, 255, 255).next();
        buffer.vertex(offset, offset, Z_SHIFT).texture(0.0f, 0.0f).color(255, 255, 255, 255).next();
        t.draw();
        RenderSystem.setShaderTexture(0, 0);
        RenderSystem.enableDepthTest();
    }

    //Navigation initialization methods

    public static ButtonWidget initButton(Text message, ButtonWidget.PressAction onPress, int positionX, int offset){
        return ButtonWidget.builder(message, onPress)
                .dimensions(positionX, offset-20, 15, 15)
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

    public static void updateMouseLocation(double mouseX, double mouseY){
        lastMouseX = mouseX;
        lastMouseY = mouseY;
    }

    public static int scaleX(double x, int offset) {
        return (int) ((x - offset * 2) * MinecraftClient.getInstance().getWindow().getScaleFactor());
    }

    public static int scaleY(double y, int offset) {
        return (int) ((y - offset * 2) * MinecraftClient.getInstance().getWindow().getScaleFactor());
    }

    public static int getUrlBoxWidth(int width, int offset){
        return width - (offset * 2) - 80;
    }

    //Browser Creation
    public static BrowserImpl createBrowser(String url, boolean transparent){
        if(MCEF.isInitialized()) {
            BrowserImpl browser = new BrowserImpl(MCEF.getClient(), url, transparent);
            browser.setCloseAllowed();
            browser.createImmediately();
            return browser;
        }else{
            throw new RuntimeException("Chromium Embedded Framework was never initialized.");
        }
    }

    //Button related Methods
    public static void openInBrowser(){
        try {
            Util.getOperatingSystem().open(new URL(currentUrl));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void reloadButtonAction(){
        instance.urlBox.setText(instance.browser.getURL());
        if(instance.browser.isLoading()) {
            instance.browser.stopLoad();
        } else {
            instance.browser.reload();
        }
    }

    public static void homeButtonAction(){
        String prediffyedHomePage = BrowserUtil.prediffyURL(MCBrowser.getConfig().homePage);
        instance.urlBox.setText(prediffyedHomePage);
        instance.browser.loadURL(prediffyedHomePage);
    }

    public static Byte toMultiBooleanByte(boolean b1, boolean b2){
        byte b = 0;
        if (b1) {
            b |= 1 << 1;
        }
        if (b2) {
            b |= 1 << 0;
        }
        return b;
    }
}
