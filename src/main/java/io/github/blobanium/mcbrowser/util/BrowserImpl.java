package io.github.blobanium.mcbrowser.util;

import com.cinemamod.mcef.MCEFBrowser;
import com.cinemamod.mcef.MCEFClient;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.blobanium.mcbrowser.MCBrowser;
import net.minecraft.client.render.*;

public class BrowserImpl extends MCEFBrowser {
    private String urlCache;
    private int tickCache;

    public BrowserImpl(MCEFClient client, String url, boolean transparent) {
        super(client, url, transparent);
        this.tickCache = MCBrowser.tickCounter;
    }

    //Improves performance by limiting each getURL call to one tick for each instance.
    @Override
    public String getURL(){
        if(urlCache == null || MCBrowser.tickCounter != tickCache) {
            String url = super.getURL();
            tickCache = MCBrowser.tickCounter;
            urlCache = url;
            return url;
        }else{
            return urlCache;
        }
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
}
