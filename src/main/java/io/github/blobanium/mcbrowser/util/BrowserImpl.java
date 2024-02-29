package io.github.blobanium.mcbrowser.util;

import com.cinemamod.mcef.MCEFBrowser;
import com.cinemamod.mcef.MCEFClient;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;

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
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        RenderSystem.setShaderTexture(0, this.getRenderer().getTextureID());
        Tessellator t = Tessellator.getInstance();
        BufferBuilder buffer = t.getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        buffer.vertex(x, y + height, Z_SHIFT).texture(0.0f, 1.0f).color(255, 255, 255, 255).next();
        buffer.vertex(x + width, y + height, Z_SHIFT).texture(1.0f, 1.0f).color(255, 255, 255, 255).next();
        buffer.vertex(x + width, y, Z_SHIFT).texture(1.0f, 0.0f).color(255, 255, 255, 255).next();
        buffer.vertex(x, y, Z_SHIFT).texture(0.0f, 0.0f).color(255, 255, 255, 255).next();
        t.draw();
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

    public void sendMousePressRelease(double mouseX, double mouseY, int button, boolean isClick) {
        if (isClick) {
            sendMousePress(BrowserScreenHelper.mouseX(mouseX, BD_OFFSET), BrowserScreenHelper.mouseY(mouseY, BD_OFFSET), button);
        } else {
            sendMouseRelease(BrowserScreenHelper.mouseX(mouseX, BD_OFFSET), BrowserScreenHelper.mouseY(mouseY, BD_OFFSET), button);
        }
    }
}
