package io.github.blobanium.mcbrowser.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

public class BrowserScreenHelper {
    private static final int Z_SHIFT = -1;

    public static void renderBrowser(int offset, int width, int height, int textureID){
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        RenderSystem.setShaderTexture(0, textureID);
        Tessellator t = Tessellator.getInstance();
        BufferBuilder buffer = t.getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        buffer.vertex(offset, height - offset, Z_SHIFT).texture(0.0f, 1.0f).color(255, 255, 255, 255).next();
        buffer.vertex(width - offset, height - offset, Z_SHIFT).texture(1.0f, 1.0f).color(255, 255, 255, 255).next();
        buffer.vertex(width - offset, offset, Z_SHIFT).texture(1.0f, 0.0f).color(255, 255, 255, 255).next();
        buffer.vertex(offset, offset, Z_SHIFT).texture(0.0f, 0.0f).color(255, 255, 255, 255).next();
        t.draw();
        RenderSystem.setShaderTexture(0, 0);
        RenderSystem.enableDepthTest();
    }
    public static int mouseX(double x, int offset) {
        return (int) ((x - offset) * MinecraftClient.getInstance().getWindow().getScaleFactor());
    }

    public static int mouseY(double y, int offset) {
        return (int) ((y - offset) * MinecraftClient.getInstance().getWindow().getScaleFactor());
    }

    public static int scaleX(double x, int offset) {
        return (int) ((x - offset * 2) * MinecraftClient.getInstance().getWindow().getScaleFactor());
    }

    public static int scaleY(double y, int offset) {
        return (int) ((y - offset * 2) * MinecraftClient.getInstance().getWindow().getScaleFactor());
    }
}
