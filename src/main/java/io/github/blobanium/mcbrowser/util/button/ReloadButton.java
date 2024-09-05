package io.github.blobanium.mcbrowser.util.button;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.blobanium.mcbrowser.util.BrowserUtil;
import io.github.blobanium.mcbrowser.util.TabManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class ReloadButton extends PressableWidget {
    private static final ButtonTextures BUTTON_TEXTURES = new ButtonTextures(
            Identifier.ofVanilla("widget/button"),
            Identifier.ofVanilla("widget/button_disabled"),
            Identifier.ofVanilla("widget/button_highlighted")
    );

    public ReloadButton(int x, int y, int width, int height) {
        super(x, y, height, width, null);
    }

    @Override
    public void onPress() {
        //Required for Implementation
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        //Required for Implementation
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        Identifier texture = BUTTON_TEXTURES.get(this.isNarratable(), this.isFocused());
        configureShaderColor(context);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();

        context.drawGuiTexture(texture, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        configureShaderColor(context);

        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        drawScrollableText(context, minecraftClient.textRenderer,
                Text.of(TabManager.getCurrentTab().isLoading() ? "❌" : "⟳"),
                this.getX() + 2, this.getY(),
                this.getX() + this.getWidth() - 2,
                this.getY() + this.getHeight(),
                16777215 | MathHelper.ceil(this.alpha * 255.0F) << 24);
    }

    private void configureShaderColor(DrawContext context) {
        context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.clicked(mouseX, mouseY)) {
            if (button == 2) {
                TabManager.copyTab(TabManager.activeTab);
                return true;
            } else {
                if (BrowserUtil.instance != null) {
                    BrowserUtil.instance.urlBox.setText(TabManager.getCurrentUrl());
                }
                reloadOrStopLoadPage();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isHovered() {
        setFocused(super.isHovered());
        return super.isHovered();
    }

    private void reloadOrStopLoadPage() {
        if (TabManager.getCurrentTab().isLoading()) {
            TabManager.getCurrentTab().stopLoad();
        } else {
            TabManager.getCurrentTab().reload();
        }
    }
}
