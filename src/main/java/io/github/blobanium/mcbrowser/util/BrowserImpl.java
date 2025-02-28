package io.github.blobanium.mcbrowser.util;

import com.cinemamod.mcef.MCEFBrowser;
import com.cinemamod.mcef.MCEFClient;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import org.lwjgl.glfw.GLFW;

import static io.github.blobanium.mcbrowser.screen.BrowserScreen.BD_OFFSET;

public class BrowserImpl extends MCEFBrowser {
    public BrowserImpl(MCEFClient client, String url, boolean transparent) {
        super(client, url, transparent);
    }

    //Improves performance by limiting each getURL call to one tick for each instance.
    @Override
    public String getURL(){
        return BrowserCaches.urlCache.getOrDefault(this.getIdentifier(), super.getURL());
    }

    @Override
    public boolean isLoading(){
        return BrowserCaches.isLoadingCache.getOrDefault(this.getIdentifier(), super.isLoading());
    }

    protected static final int Z_SHIFT = -1;

    public void render(int x, int y, int width, int height) {
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture(0, this.getRenderer().getTextureID());
        Tessellator t = Tessellator.getInstance();
        BufferBuilder buffer = t.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        buffer.vertex(x, y + height, Z_SHIFT).texture(0.0f, 1.0f).color(255, 255, 255, 255);
        buffer.vertex(x + width, y + height, Z_SHIFT).texture(1.0f, 1.0f).color(255, 255, 255, 255);
        buffer.vertex(x + width, y, Z_SHIFT).texture(1.0f, 0.0f).color(255, 255, 255, 255);
        buffer.vertex(x, y, Z_SHIFT).texture(0.0f, 0.0f).color(255, 255, 255, 255);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.setShaderTexture(0, 0);
        RenderSystem.enableDepthTest();
    }

    public void sendKeyPressRelease(int keyCode, int scanCode, int modifiers, boolean isPress){
        if(isPress){
            sendKeyPress(keyCode, scanCode, modifiers);
        }else{
            sendKeyRelease(keyCode, scanCode, modifiers);
        }
    }

    public void mouseButtonControl(double mouseX, double mouseY, int button, boolean isClick) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_4 && canGoBack() && !isClick) {
                goBack();
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_5 && canGoForward() && !isClick) {
                goForward();
            } else {
                BrowserUtil.runAsyncIfEnabled(() -> {
                    if (isClick) {
                        sendMousePress(BrowserUtil.mouseX(mouseX, BD_OFFSET), BrowserUtil.mouseY(mouseY, BD_OFFSET), button);
                    } else {
                        sendMouseRelease(BrowserUtil.mouseX(mouseX, BD_OFFSET), BrowserUtil.mouseY(mouseY, BD_OFFSET), button);
                    }
                });
            }
    }
}
