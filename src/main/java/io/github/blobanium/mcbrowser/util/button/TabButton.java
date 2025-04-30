package io.github.blobanium.mcbrowser.util.button;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.blobanium.mcbrowser.MCBrowser;
import io.github.blobanium.mcbrowser.screen.BrowserScreen;
import io.github.blobanium.mcbrowser.util.BrowserUtil;
import io.github.blobanium.mcbrowser.util.TabManager;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
public class TabButton extends PressableWidget {
    private static final ButtonTextures TEXTURES = new ButtonTextures(
            Identifier.ofVanilla("widget/button"), Identifier.ofVanilla("widget/button_disabled"), Identifier.ofVanilla("widget/button_highlighted")
    );
    int tab;
    final int startX;

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
        //Required For Implementation
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        //Required For Implementation
    }

    private final boolean selected = TabManager.activeTab == tab;
    private final boolean tooSmall = this.getWidth() < this.getHeight() * 3;

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        Identifier texture = TEXTURES.get(this.isNarratable(), this.isFocused());
        RenderLayer.getGuiTextured(texture);

        if (this.getX() > BrowserUtil.instance.width - BrowserScreen.BD_OFFSET - 35) { return; }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        if (this.getWidth() > this.getHeight()) { context.drawGuiTexture(RenderLayer.GUI_TEXTURED, texture, this.getX(), this.getY(), this.getWidth(), this.getHeight()); }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        String name = TabManager.tabs.get(tab).getTitle();
        if (name == null || name.isEmpty()) { name = "Loading..."; }
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        drawScrollableText(context, textRenderer, Text.of(name), this.getX() + 2 + 15, this.getY(), this.getX() + this.getWidth() - (!tooSmall || selected ? 17 : 2), this.getY() + this.getHeight(), 16777215 | MathHelper.ceil(this.alpha * 255.0F) << 24);

        context.fill(this.getX(), this.getY(), this.getX() + this.getHeight(), this.getY() + this.getHeight(), 0xFFFFFFFF);
        renderIco();
        if (!tooSmall || selected) {
            context.drawGuiTexture(RenderLayer.GUI_TEXTURED, texture, this.getX() + (this.getWidth() - 15), this.getY(), 15, this.getHeight());
            String cross = "❌";
            context.drawText(textRenderer, cross, this.getX() + this.getWidth() - 8 - textRenderer.getWidth(cross) / 2, this.getY() + 4, 0xFFFFFFFF, true);
        }

        if (selected) { context.fill(this.getX(), this.getY() + this.getHeight(), this.getX() + this.getWidth(), this.getY() + this.getHeight() + 2, 0xFFFFFFFF); }
    }

    public void renderIco() {
        BrowserTabIcon ico = TabManager.tabs.get(tab).getIcon();
        if (ico == null) {
            initIco();
            return;
        }
        if (!TabManager.tabs.get(tab).isInit()) {
            ico.render(this.getX() + 1, this.getY() + 1, this.getHeight() - 2, this.getHeight() - 2);
            return;
        }
        String browserUrl = TabManager.tabs.get(tab).getBrowser().getURL();
        String icoUrl = ico.getURL();
        if (!icoUrl.isEmpty() && !browserUrl.isEmpty()) {
            if(icoUrl.endsWith(browserUrl) || isIcoForUrl(icoUrl, browserUrl)){
                ico.render(this.getX() + 1, this.getY() + 1, this.getHeight() - 2, this.getHeight() - 2);
            } else {
                resetIco();
            }
        }
    }

    private boolean isIcoForUrl(String icoUrl, String url) {
        int end = icoUrl.length();
        int begin = 0;
        if (icoUrl.contains("&size=")) {
            end = icoUrl.lastIndexOf("&size=");
        }
        if (icoUrl.contains("&url=")) {
            begin = icoUrl.lastIndexOf("&url=") + 5;
        }
        icoUrl = icoUrl.substring(begin, end);
        if (icoUrl.contains("://")) {
            icoUrl = icoUrl.substring(icoUrl.indexOf("://") + 3);
        }

        String siteUrl = url;
        if (siteUrl.contains("://")) {
            siteUrl = siteUrl.substring(siteUrl.indexOf("://") + 3);
        }
        if (siteUrl.contains("/")) {
            siteUrl = siteUrl.substring(0, siteUrl.indexOf('/'));
        }
        return icoUrl.startsWith(siteUrl);
    }

    private void initIco() {
        String currentUrl = TabManager.tabs.get(tab).getUrl();
        if (currentUrl.isEmpty()) {
            return;
        }
        TabManager.tabs.get(tab).initIcon(currentUrl);
        CompletableFuture.runAsync(() -> {
            try {
                BufferedImage bufferedImage = ImageIO.read(new URI(BrowserTabIcon.API_URL + currentUrl).toURL());
                TabManager.tabs.get(tab).getIcon().setSize(bufferedImage.getWidth());
            } catch (IOException | URISyntaxException e) {
                MCBrowser.LOGGER.warn("Could not find size of ico for {}", currentUrl, e);
            }
        });
    }

    public void resetIco() {
        TabManager.tabs.get(tab).resetIcon();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if ((this.getX() > BrowserUtil.instance.width - BrowserScreen.BD_OFFSET - 35) || !this.isSelected()) {
            return false;
        }

        if (button == 2) {
            close();
            return true;
        }

        if (tooSmall && !selected) {
            open();
            return true;
        }

        if (mouseX <= this.getX() + this.getWidth() - 15) open(); else close();
        return true;
    }

    public void open() {
        TabManager.setActiveTab(tab);
    }

    public void close() {
        TabManager.closeTab(tab);
    }

    @Override
    public boolean isHovered(){
        setFocused(super.isHovered());
        return super.isHovered();
    }
}
