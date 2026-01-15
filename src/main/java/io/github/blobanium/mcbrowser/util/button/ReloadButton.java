package io.github.blobanium.mcbrowser.util.button;

import io.github.blobanium.mcbrowser.util.BrowserUtil;
import io.github.blobanium.mcbrowser.util.TabManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.input.AbstractInput;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class ReloadButton extends PressableWidget {
    private static final ButtonTextures TEXTURES = new ButtonTextures(
            Identifier.ofVanilla("widget/button"), Identifier.ofVanilla("widget/button_disabled"), Identifier.ofVanilla("widget/button_highlighted")
    );

    public ReloadButton(int x, int y, int width, int height) {
        super(x, y, height, width, Text.empty());
    }

    @Override
    public void onPress(AbstractInput input) {
        //Required for Implementation
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        //Required for Implementation
    }

    @Override
    protected void drawIcon(DrawContext context, int x, int y, float delta) {
        Identifier texture = TEXTURES.get(this.isInteractable(), this.isFocused());
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, texture, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        int color = 16777215 | MathHelper.ceil(this.alpha * 255.0F) << 24;
        context.drawCenteredTextWithShadow(minecraftClient.textRenderer, Text.of(TabManager.getCurrentTab().isLoading() ? "❌" : "⟳"), this.getX() + this.getWidth() / 2, this.getY() + (this.getHeight() - 8) / 2, color);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (!this.isSelected()) {
            return false;
        }

        if (click.button() == 2) {
            TabManager.copyTab(TabManager.activeTab);
        } else {
            if (BrowserUtil.instance != null) {
                BrowserUtil.instance.urlBox.setText(TabManager.getCurrentTab().getURL());
            }
            reloadOrStopLoadPage();
        }
        return true;
    }

    @Override
    public boolean isHovered() {
        setFocused(super.isHovered());
        return super.isHovered();
    }

    public void reloadOrStopLoadPage() {
        if (TabManager.getCurrentTab().isLoading()) {
            TabManager.getCurrentTab().stopLoad();
        } else {
            TabManager.getCurrentTab().reload();
        }
    }
}
