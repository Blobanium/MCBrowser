package io.github.blobanium.mcbrowser.util;

import com.cinemamod.mcef.MCEFBrowser;
import com.cinemamod.mcef.MCEFClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.lwjgl.glfw.GLFW;

import static io.github.blobanium.mcbrowser.screen.BrowserScreen.BD_OFFSET;

public class BrowserImpl extends MCEFBrowser {

    public BrowserImpl(MCEFClient client, String url, boolean transparent) {
        super(client, url, transparent);
    }

    //Improves performance by limiting each getURL call to one tick for each instance.
    @Override
    public String getURL() {
        return BrowserCaches.urlCache.getOrDefault(this.getIdentifier(), super.getURL());
    }

    @Override
    public boolean isLoading() {
        return BrowserCaches.isLoadingCache.getOrDefault(this.getIdentifier(), super.isLoading());
    }

    public void render(DrawContext context, int x, int y, int width, int height) {
        Identifier textureLocation = getTextureLocation();

        if (isTextureReady()) {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, textureLocation, x, y, 0F, 0F, width, height, width, height, ColorHelper.getWhite(1F));
        }
    }

    public void sendKeyPressRelease(int keyCode, int scanCode, int modifiers, boolean isPress) {
        if (isPress) {
            sendKeyPress(keyCode, scanCode, modifiers);
        } else {
            sendKeyRelease(keyCode, scanCode, modifiers);
        }
    }

    public void mouseButtonControl(Click click, boolean isDouble, boolean isClick) {
        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_4 && canGoBack() && !isClick) {
            goBack();
        } else if (click.button() == GLFW.GLFW_MOUSE_BUTTON_5 && canGoForward() && !isClick) {
            goForward();
        } else {
            BrowserUtil.runAsyncIfEnabled(() -> {
                if (isClick) {
                    sendMousePress(BrowserUtil.mouseX(click.x(), BD_OFFSET), BrowserUtil.mouseY(click.y(), BD_OFFSET), click.button());
                } else {
                    sendMouseRelease(BrowserUtil.mouseX(click.x(), BD_OFFSET), BrowserUtil.mouseY(click.y(), BD_OFFSET), click.button());
                }
            });
        }
    }
}
