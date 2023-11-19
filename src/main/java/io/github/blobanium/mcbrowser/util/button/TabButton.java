package io.github.blobanium.mcbrowser.util.button;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.blobanium.mcbrowser.MCBrowser;
import io.github.blobanium.mcbrowser.screen.BrowserScreen;
import io.github.blobanium.mcbrowser.util.BrowserScreenHelper;
import me.shedaniel.math.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import static io.github.blobanium.mcbrowser.MCBrowser.*;

public class TabButton extends PressableWidget {
    int tab;
    int startX;

    public TabButton(int startX, int y, int width, int height, int tab) {
        super(0, y, width, height, null);
        this.startX = startX;
        this.tab = tab;
    }

    @Override
    public int getX() {
        return startX + (tab * (getWidth() + 5));
    }

    public void setTab(int tab) {
        this.tab = tab;
    }


    @Override
    public void onPress() {
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }

    @Override
    public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.getX() > BrowserScreenHelper.instance.width - BrowserScreen.BROWSER_DRAW_OFFSET - 35) {
            return;
        }
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        context.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        boolean selected = activeTab == tab;
        boolean hoveringClose = (mouseX >= this.getX() + (this.getWidth() - 15)) && (mouseX <= this.getX() + this.getWidth()) && (mouseY >= this.getY()) && (mouseY <= this.getY() + this.getHeight());
        boolean tooSmall = this.getWidth() < this.getHeight() * 3;
        int mainTextureOffset = selected ? 2 : 1;
        int closeTextureOffset = hoveringClose ? 2 : 1;
        if (this.getWidth() > this.getHeight()) {
            context.drawNineSlicedTexture(WIDGETS_TEXTURE, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 20, 4, 200, 20, 0, 46 + mainTextureOffset * 20);
        }
        context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        String name;
        if (tabs.get(tab).isInit()) {
            name = tabs.get(tab).getBrowser().getURL();
            if (!name.isEmpty()) {
                if (name.startsWith("https://")) {
                    name = name.substring(8);
                }
                if (name.startsWith("www.")) {
                    name = name.substring(4);
                }
                if (name.endsWith("/")) {
                    name = name.substring(0, name.length() - 1);
                }
            } else {
                name = "Loading...";
            }
        } else {
            name = tabs.get(tab).holderUrl;
        }
        drawScrollableText(context, textRenderer, Text.of(name), this.getX() + 2 + 15, this.getY(), this.getX() + this.getWidth() - (!tooSmall || selected ? 17 : 2), this.getY() + this.getHeight(), 16777215 | MathHelper.ceil(this.alpha * 255.0F) << 24);

        context.fill(this.getX(), this.getY(), this.getX() + this.getHeight(), this.getY() + this.getHeight(), 0xFFFFFFFF);
        renderIco();
        if (!tooSmall || selected) {
            context.drawNineSlicedTexture(WIDGETS_TEXTURE, this.getX() + (this.getWidth() - 15), this.getY(), 15, this.getHeight(), 20, 4, 200, 20, 0, 46 + closeTextureOffset * 20);
            String cross = "\u274C";
            context.drawText(textRenderer, cross, this.getX() + this.getWidth() - 8 - textRenderer.getWidth(cross) / 2, this.getY() + 4, 0xFFFFFFFF, true);
        }

        if (selected) {
            context.fill(this.getX(), this.getY() + this.getHeight(), this.getX() + this.getWidth(), this.getY() + this.getHeight() + 2, 0xFFFFFFFF);
        }
    }

    public void renderIco() {
        BrowserTabIcon ico = tabs.get(tab).getIcon();
        if (ico != null) {
            if (!tabs.get(tab).isInit()) {
                ico.render(this.getX() + 1, this.getY() + 1, this.getHeight() - 2, this.getHeight() - 2);
                return;
            }
            if (!ico.getURL().isEmpty() && !tabs.get(tab).getBrowser().getURL().isEmpty()) {
                String icoUrl = ico.getURL().replace("+", "%20");
                if (!icoUrl.endsWith(tabs.get(tab).getBrowser().getURL())) {
                    if (isIcoForUrl(icoUrl, tabs.get(tab).getBrowser().getURL())) {
                        ico.render(this.getX() + 1, this.getY() + 1, this.getHeight() - 2, this.getHeight() - 2);
                        return;
                    }
                    resetIco();
                }
            }
        } else {
            initIco();
        }
    }

    private boolean isIcoForUrl(String icoUrl, String url) {
        if (icoUrl.contains("&size=")) {
            icoUrl = icoUrl.substring(0, icoUrl.lastIndexOf("&size="));
        }
        return icoUrl.endsWith(url);
    }

    private void initIco() {
        String currentUrl;
        if (!tabs.get(tab).isInit()) {
            currentUrl = tabs.get(tab).holderUrl;
        } else {
            currentUrl = tabs.get(tab).getBrowser().getURL();
        }
        if (currentUrl.isEmpty()) {
            return;
        }
        tabs.get(tab).initIcon(currentUrl);
        try {
            BufferedImage bufferedImage = ImageIO.read(new URL(BrowserTabIcon.apiUrl + currentUrl));
            tabs.get(tab).getIcon().setSize(bufferedImage.getWidth());
        } catch (IOException e) {
            MCBrowser.LOGGER.warn("Could not find size of ico for " + currentUrl);
        }
    }

    public void resetIco() {
        tabs.get(tab).resetIcon();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.getX() > BrowserScreenHelper.instance.width - BrowserScreen.BROWSER_DRAW_OFFSET - 35) {
            return false;
        }
        if (this.clicked(mouseX, mouseY)) {
            if (button == 2) {
                close();
                return true;
            }
            if (this.getWidth() < this.getHeight() * 3) {
                if (activeTab != tab) {
                    open();
                    return true;
                }
            }
            if (mouseX > this.getX() + this.getWidth() - 15) {
                close();
            } else {
                open();
            }
            return true;
        }
        return false;
    }

    public void open() {
        setActiveTab(tab);
    }

    public void close() {
        closeTab(tab);
    }
}
