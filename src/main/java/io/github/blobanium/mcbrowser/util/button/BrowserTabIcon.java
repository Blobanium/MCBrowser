package io.github.blobanium.mcbrowser.util.button;

import com.cinemamod.mcef.MCEFClient;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.blobanium.mcbrowser.util.BrowserImpl;
import net.minecraft.client.render.*;

public class BrowserTabIcon extends BrowserImpl {
    static final String apiUrl = "https://www.google.com/s2/favicons?sz=64&domain_url=";
    //TODO maybe replace the apiUrl thing with https://besticon-demo.herokuapp.com/allicons.json?url=URL,
    // cause it can help changing size of rendered field to match size of icon
    int size = 64;

    public BrowserTabIcon(MCEFClient client, String url, boolean transparent) {
        super(client, apiUrl + url, transparent);
        setSize(size);
    }

    @Override
    public void render(int x, int y, int width, int height) {
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        RenderSystem.setShaderTexture(0, this.getRenderer().getTextureID());
        Tessellator t = Tessellator.getInstance();
        BufferBuilder buffer = t.getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        buffer.vertex(x, height + y, Z_SHIFT).texture(0.0f, 1.0f).color(255, 255, 255, 255).next();
        buffer.vertex(width + x, height + y, Z_SHIFT).texture(1.0f, 1.0f).color(255, 255, 255, 255).next();
        buffer.vertex(width + x, y, Z_SHIFT).texture(1.0f, 0.0f).color(255, 255, 255, 255).next();
        buffer.vertex(x, y, Z_SHIFT).texture(0.0f, 0.0f).color(255, 255, 255, 255).next();
        t.draw();
        RenderSystem.setShaderTexture(0, 0);
        RenderSystem.enableDepthTest();
    }

    public void setSize(int size) {
        this.size = size;
        this.resize(this.size, this.size);
    }
}
