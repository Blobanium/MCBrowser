package io.github.blobanium.mcbrowser.util.button;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.blobanium.mcbrowser.util.BrowserScreenHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import static io.github.blobanium.mcbrowser.MCBrowser.*;

public class ReloadButton extends PressableWidget {
    public ReloadButton(int x, int y, int width, int height) {
        super(x, y, height, width, null);
    }

    @Override
    public void onPress() {

    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    @Override
    protected void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        context.drawNineSlicedTexture(WIDGETS_TEXTURE, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 20, 4, 200, 20, 0, 46 + (this.isSelected() ? 2 : 1) * 20);
        context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        drawScrollableText(context, minecraftClient.textRenderer, Text.of(getCurrentTab().isLoading() ? "\u274C" : "\u27F3"), this.getX() + 2, this.getY(), this.getX() + this.getWidth() - 2, this.getY() + this.getHeight(), 16777215 | MathHelper.ceil(this.alpha * 255.0F) << 24);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.clicked(mouseX, mouseY)) {
            if (button == 2) {
                copyTab(activeTab);
                return true;
            } else {
                if (BrowserScreenHelper.instance != null) {
                    BrowserScreenHelper.instance.urlBox.setText(getCurrentTab().getURL());
                }
                reloadOrStopLoadPage();
            }
            return true;
        }
        return false;
    }

    public void reloadOrStopLoadPage() {
        if (getCurrentTab().isLoading()) {
            getCurrentTab().stopLoad();
        } else {
            getCurrentTab().reload();
        }
    }
}
