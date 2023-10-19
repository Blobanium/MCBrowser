package io.github.blobanium.mcbrowser.screen;

import com.cinemamod.mcef.MCEF;
import com.cinemamod.mcef.MCEFBrowser;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.blobanium.mcbrowser.MCBrowser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.text.Text;

import java.util.function.Supplier;

public class BrowserScreen extends Screen {
    private static final int BROWSER_DRAW_OFFSET = 50;
    private static final int Z_SHIFT = -1;
    private static final int URL_GAP = 20;

    private MCEFBrowser browser;

    private MinecraftClient minecraft = MinecraftClient.getInstance();


    private TextFieldWidget urlBox;
    public BrowserScreen(Text title) {
        super(title);
    }


    @Override
    protected void init() {
        super.init();
        if (browser == null) {
            String url = "https://www.google.com";
            boolean transparent = true;
            browser = MCEF.createBrowser(url, transparent);
            resizeBrowser();
            this.urlBox = new TextFieldWidget(minecraft.textRenderer, BROWSER_DRAW_OFFSET,BROWSER_DRAW_OFFSET-20,width-(BROWSER_DRAW_OFFSET*2),15, Text.of("TEST1234"));
            this.urlBox.setEditable(true);
        }
    }

    private int mouseX(double x) {
        return (int) ((x - BROWSER_DRAW_OFFSET) * minecraft.getWindow().getScaleFactor());
    }

    private int mouseY(double y) {
        return (int) ((y - BROWSER_DRAW_OFFSET) * minecraft.getWindow().getScaleFactor());
    }

    private int scaleX(double x) {
        return (int) ((x - BROWSER_DRAW_OFFSET * 2) * minecraft.getWindow().getScaleFactor());
    }

    private int scaleY(double y) {
        return (int) ((y - BROWSER_DRAW_OFFSET * 2) * minecraft.getWindow().getScaleFactor());
    }

    private void resizeBrowser() {
        if (width > 100 && height > 100) {
            browser.resize(scaleX(width), scaleY(height));
        }
        if(this.urlBox != null) {
            urlBox.setWidth(width - (BROWSER_DRAW_OFFSET * 2));
        }
    }

    @Override
    public void resize(MinecraftClient minecraft, int i, int j) {
        super.resize(minecraft, i, j);
        resizeBrowser();
    }

    @Override
    public void close() {
        browser.close();
        MCBrowser.requestOpen = false;
        super.close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        urlBox.renderButton(context, mouseX, mouseY, delta);
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        RenderSystem.setShaderTexture(0, browser.getRenderer().getTextureID());
        Tessellator t = Tessellator.getInstance();
        BufferBuilder buffer = t.getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        buffer.vertex(BROWSER_DRAW_OFFSET, height - BROWSER_DRAW_OFFSET, Z_SHIFT).texture(0.0f, 1.0f).color(255, 255, 255, 255).next();
        buffer.vertex(width - BROWSER_DRAW_OFFSET, height - BROWSER_DRAW_OFFSET, Z_SHIFT).texture(1.0f, 1.0f).color(255, 255, 255, 255).next();
        buffer.vertex(width - BROWSER_DRAW_OFFSET, BROWSER_DRAW_OFFSET, Z_SHIFT).texture(1.0f, 0.0f).color(255, 255, 255, 255).next();
        buffer.vertex(BROWSER_DRAW_OFFSET, BROWSER_DRAW_OFFSET, Z_SHIFT).texture(0.0f, 0.0f).color(255, 255, 255, 255).next();
        t.draw();
        RenderSystem.setShaderTexture(0, 0);
        RenderSystem.enableDepthTest();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        urlBox.onClick(mouseX, mouseY);
        browser.sendMousePress(mouseX(mouseX), mouseY(mouseY), button);
        browser.setFocus(true);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        urlBox.onRelease(mouseX, mouseY);
        browser.sendMouseRelease(mouseX(mouseX), mouseY(mouseY), button);
        browser.setFocus(true);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        urlBox.mouseMoved(mouseX, mouseY);
        browser.sendMouseMove(mouseX(mouseX), mouseY(mouseY));
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        urlBox.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        browser.sendMouseWheel(mouseX(mouseX), mouseY(mouseY), delta, 0);
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        urlBox.keyPressed(keyCode, scanCode, modifiers);
        browser.sendKeyPress(keyCode, scanCode, modifiers);
        browser.setFocus(true);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        urlBox.keyReleased(keyCode, scanCode, modifiers);
        browser.sendKeyRelease(keyCode, scanCode, modifiers);
        browser.setFocus(true);
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (codePoint == (char) 0) return false;
        urlBox.charTyped(codePoint, modifiers);
        browser.sendKeyTyped(codePoint, modifiers);
        browser.setFocus(true);
        return super.charTyped(codePoint, modifiers);
    }
}
