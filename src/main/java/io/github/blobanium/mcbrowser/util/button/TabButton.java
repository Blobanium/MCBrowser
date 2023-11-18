package io.github.blobanium.mcbrowser.util.button;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.blobanium.mcbrowser.MCBrowser;
import io.github.blobanium.mcbrowser.util.BrowserScreenHelper;
import net.minecraft.client.MinecraftClient;
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
        return startX + (tab * (width + 5));
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
        context.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        int mainTextureOffset = 1;
        int closeTextureOffset = 1;
        if (activeTab == tab) {
            mainTextureOffset = 2;
        }
        if ((mouseX >= this.getX() + (this.getWidth() - 15)) && (mouseX <= this.getX() + this.getWidth()) && (mouseY >= this.getY()) && (mouseY <= this.getY() + this.getHeight())) {
            closeTextureOffset = 2;
        }
        context.fill(this.getX(), this.getY(), this.getX() + this.getHeight(), this.getY() + this.getHeight(), 0xFFFFFFFF);
        context.drawNineSlicedTexture(WIDGETS_TEXTURE, this.getX() + this.getHeight(), this.getY(), this.getWidth() - this.getHeight() - 15, this.getHeight(), 20, 4, 200, 20, 0, 46 + mainTextureOffset * 20);
        context.drawNineSlicedTexture(WIDGETS_TEXTURE, this.getX() + (this.getWidth() - 15), this.getY(), 15, this.getHeight(), 20, 4, 200, 20, 0, 46 + closeTextureOffset * 20);
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
        drawScrollableText(context, MinecraftClient.getInstance().textRenderer, Text.of(name), this.getX() + 2 + 15, this.getY(), this.getX() + this.getWidth() - 15 - 2, this.getY() + this.getHeight(), 16777215 | MathHelper.ceil(this.alpha * 255.0F) << 24);
        renderIco();
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
        if (this.clicked(mouseX, mouseY)) {
            if (button == 2) {
                close();
                return true;
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
        BrowserScreenHelper.instance.removeTab(tab);
        tabs.get(tab).close();
        tabs.remove(tab);
        if (tabs.size() == 0) {
            BrowserScreenHelper.instance.close();
            return;
        }
        if (tab <= activeTab && activeTab != 0) {
            setActiveTab(activeTab - 1);
        } else {
            BrowserScreenHelper.instance.updateWidgets();
        }
    }
}
