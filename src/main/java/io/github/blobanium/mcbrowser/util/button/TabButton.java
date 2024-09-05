package io.github.blobanium.mcbrowser.util.button;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.blobanium.mcbrowser.MCBrowser;
import io.github.blobanium.mcbrowser.screen.BrowserScreen;
import io.github.blobanium.mcbrowser.util.BrowserUtil;
import io.github.blobanium.mcbrowser.util.TabManager;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class TabButton extends PressableWidget {
    private static final ButtonTextures TEXTURES = new ButtonTextures(
            Identifier.ofVanilla("widget/button"),
            Identifier.ofVanilla("widget/button_disabled"),
            Identifier.ofVanilla("widget/button_highlighted")
    );

    private static final int ICON_SIZE_OFFSET = 2;
    private static final int CROSS_SIZE = 15;
    private static final int BD_OFFSET_THRESHOLD = 35;
    private static final int NARRATION_ALPHA = 255;
    private static final String LOADING_TEXT = "Loading...";
    private static final String CROSS_TEXT = "❌";

    private int tab;
    private final int startX;

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
        if (isButtonOffScreen()) return;

        setupRenderingContext(context);

        if (this.getWidth() > this.getHeight()) {
            context.drawGuiTexture(texture, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        }

        String tabTitle = getTabTitle();
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        drawScrollableText(context, textRenderer, Text.of(tabTitle), this.getX() + CROSS_SIZE, this.getY(),
                this.getX() + this.getWidth() - getOffset(), this.getY() + this.getHeight(), calculateColor());

        context.fill(this.getX(), this.getY(), this.getX() + this.getHeight(), this.getY() + this.getHeight(), 0xFFFFFFFF);
        renderIcon(context);

        if (!tooSmall || selected) {
            context.drawGuiTexture(texture, this.getX() + (this.getWidth() - CROSS_SIZE), this.getY(), CROSS_SIZE, this.getHeight());
            drawCross(context, textRenderer);
        }

        if (selected) {
            context.fill(this.getX(), this.getY() + this.getHeight(), this.getX() + this.getWidth(), this.getY() + this.getHeight() + ICON_SIZE_OFFSET, 0xFFFFFFFF);
        }
    }

    private boolean isButtonOffScreen() {
        return this.getX() > BrowserUtil.instance.width - BrowserScreen.BD_OFFSET - BD_OFFSET_THRESHOLD;
    }

    private void setupRenderingContext(DrawContext context) {
        context.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
    }

    private String getTabTitle() {
        String name = TabManager.tabs.get(tab).getTitle();
        return (name == null || name.isEmpty()) ? LOADING_TEXT : name;
    }

    private int getOffset() {
        return (!tooSmall || selected) ? 17 : 2;
    }

    private int calculateColor() {
        return 16777215 | MathHelper.ceil(this.alpha * NARRATION_ALPHA) << 24;
    }

    private void drawCross(DrawContext context, TextRenderer textRenderer) {
        int xPos = this.getX() + this.getWidth() - 8 - textRenderer.getWidth(CROSS_TEXT) / 2;
        context.drawText(textRenderer, CROSS_TEXT, xPos, this.getY() + 4, 0xFFFFFFFF, true);
    }

    private void renderIcon(DrawContext context) {
        BrowserTabIcon ico = TabManager.tabs.get(tab).getIcon();
        if (ico == null) {
            initIcon();
            return;
        }
        if (!TabManager.tabs.get(tab).isInit()) {
            ico.render(this.getX() + 1, this.getY() + 1, this.getHeight() - 2, this.getHeight() - 2);
            return;
        }
        String browserUrl = TabManager.tabs.get(tab).getBrowser().getURL();
        String icoUrl = ico.getURL();
        if (!icoUrl.isEmpty() && !browserUrl.isEmpty()) {
            if (!icoUrl.endsWith(browserUrl)) {
                if (isIconForUrl(icoUrl, browserUrl)) {
                    ico.render(this.getX() + 1, this.getY() + 1, this.getHeight() - 2, this.getHeight() - 2);
                    return;
                }
                resetIcon();
            } else {
                ico.render(this.getX() + 1, this.getY() + 1, this.getHeight() - 2, this.getHeight() - 2);
            }
        }
    }

    private boolean isIconForUrl(String icoUrl, String url) {
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

    private void initIcon() {
        String currentUrl = TabManager.tabs.get(tab).getUrl();
        if (currentUrl.isEmpty()) {
            return;
        }
        TabManager.tabs.get(tab).initIcon(currentUrl);
        CompletableFuture.runAsync(() -> {
            try {
                BufferedImage bufferedImage = ImageIO.read(new URL(BrowserTabIcon.API_URL + currentUrl));
                TabManager.tabs.get(tab).getIcon().setSize(bufferedImage.getWidth());
            } catch (IOException e) {
                MCBrowser.LOGGER.warn("Could not find size of icon for " + currentUrl);
            }
        });
    }

    public void resetIcon() {
        TabManager.tabs.get(tab).resetIcon();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isButtonOffScreen()) {
            return false;
        }
        if (this.clicked(mouseX, mouseY)) {
            if (button == 2) {
                close();
                return true;
            }
            if (tooSmall && !selected) {
                open();
                return true;
            }
            toggleOpenClose(!(mouseX > this.getX() + this.getWidth() - CROSS_SIZE));
            return true;
        }
        return false;
    }

    private void toggleOpenClose(boolean action) {
        if (action) {
            open();
        } else {
            close();
        }
    }

    public void open() {
        TabManager.setActiveTab(tab);
    }

    public void close() {
        TabManager.closeTab(tab);
    }

    @Override
    public boolean isHovered() {
        setFocused(super.isHovered());
        return super.isHovered();
    }
}
