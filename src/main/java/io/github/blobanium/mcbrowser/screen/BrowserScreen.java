package io.github.blobanium.mcbrowser.screen;

import com.cinemamod.mcef.MCEF;
import com.cinemamod.mcef.MCEFBrowser;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.blobanium.mcbrowser.BrowserUtil;
import io.github.blobanium.mcbrowser.MCBrowser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.text.Text;


public class BrowserScreen extends Screen {
    private static final int BROWSER_DRAW_OFFSET = 50;
    private static final int Z_SHIFT = -1;
    private static final int ENTER_KEY_CODE = 257;

    private MCEFBrowser browser;

    private MinecraftClient minecraft = MinecraftClient.getInstance();

    //URL
    private String initURL;
    private String currentUrl;


    //Ui
    private TextFieldWidget urlBox;
    private ButtonWidget forwardButton;
    private ButtonWidget backButton;
    private ButtonWidget reloadButton;

    //Mouse Position
    private double lastMouseX;
    private double lastMouseY;

    public BrowserScreen(Text title, String url) {
        super(title);
        this.initURL = url;
    }


    @Override
    protected void init() {
        super.init();
        if (browser == null) {
            boolean transparent = true;
            browser = MCEF.createBrowser(this.initURL, transparent);
            resizeBrowser();

            this.urlBox = new TextFieldWidget(minecraft.textRenderer, BROWSER_DRAW_OFFSET + 60,BROWSER_DRAW_OFFSET-20,(width-(BROWSER_DRAW_OFFSET*2))-60,15, Text.of("TEST1234")){
                @Override
                public boolean keyPressed(int keyCode, int scanCode, int modifiers){
                    if(isFocused()) {
                        browser.setFocus(false);
                        if(keyCode == ENTER_KEY_CODE){
                            browser.loadURL(BrowserUtil.prediffyURL(getText()));
                            setFocused(false);
                        }
                    }
                    return super.keyPressed(keyCode, scanCode, modifiers);
                }
            };
            urlBox.setMaxLength(2048); //Most browsers have a max length of 2048
            urlBox.setText(Text.of("").getString());
            addSelectableChild(urlBox);

            backButton = new ButtonWidget.Builder(Text.of("\u25C0"), button -> browser.goBack())
                    .dimensions(BROWSER_DRAW_OFFSET, BROWSER_DRAW_OFFSET-20, 15, 15)
                    .build();
            addSelectableChild(backButton);

            forwardButton = ButtonWidget.builder(Text.of("\u25B6"), button -> browser.goForward())
                    .dimensions(BROWSER_DRAW_OFFSET + 20, BROWSER_DRAW_OFFSET-20, 15, 15)
                    .build();
            addSelectableChild(forwardButton);

            reloadButton = ButtonWidget.builder(Text.of("\u27F3"), button -> {
                        if(browser.isLoading()){
                            browser.stopLoad();
                        }else{
                            browser.reload();
                        }
                    })
                    .dimensions(BROWSER_DRAW_OFFSET + 40, BROWSER_DRAW_OFFSET-20, 15, 15)
                    .build();
            addSelectableChild(reloadButton);
        }
    }

    private int mouseX(double x) {
        return (int) ((x - BROWSER_DRAW_OFFSET) * minecraft.getWindow().getScaleFactor());
    }

    private int mouseY(double y) {
        return (int) ((y - BROWSER_DRAW_OFFSET) * minecraft.getWindow().getScaleFactor());
    }

    private int scaleX(double x) {
        return (int) ((x - BROWSER_DRAW_OFFSET * 2) * minecraft.getWindow().getScaleFactor());
    }

    private int scaleY(double y) {
        return (int) ((y - BROWSER_DRAW_OFFSET * 2) * minecraft.getWindow().getScaleFactor());
    }

    private void resizeBrowser() {
        if (width > 100 && height > 100) {
            browser.resize(scaleX(width), scaleY(height));
        }
        if(this.urlBox != null) {
            urlBox.setWidth(width - (BROWSER_DRAW_OFFSET * 2) - 60);
        }
    }

    @Override
    public void resize(MinecraftClient minecraft, int i, int j) {
        super.resize(minecraft, i, j);
        resizeBrowser();
        reloadAllChildren();
    }

    @Override
    public void close() {
        browser.close();
        MCBrowser.requestOpen = false;
        super.close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        RenderSystem.setShaderTexture(0, browser.getRenderer().getTextureID());
        Tessellator t = Tessellator.getInstance();
        BufferBuilder buffer = t.getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        buffer.vertex(BROWSER_DRAW_OFFSET, height - BROWSER_DRAW_OFFSET, Z_SHIFT).texture(0.0f, 1.0f).color(255, 255, 255, 255).next();
        buffer.vertex(width - BROWSER_DRAW_OFFSET, height - BROWSER_DRAW_OFFSET, Z_SHIFT).texture(1.0f, 1.0f).color(255, 255, 255, 255).next();
        buffer.vertex(width - BROWSER_DRAW_OFFSET, BROWSER_DRAW_OFFSET, Z_SHIFT).texture(1.0f, 0.0f).color(255, 255, 255, 255).next();
        buffer.vertex(BROWSER_DRAW_OFFSET, BROWSER_DRAW_OFFSET, Z_SHIFT).texture(0.0f, 0.0f).color(255, 255, 255, 255).next();
        t.draw();
        RenderSystem.setShaderTexture(0, 0);
        RenderSystem.enableDepthTest();
        urlBox.renderButton(context, mouseX, mouseY, delta);
        backButton.render(context, mouseX, mouseY, delta);
        forwardButton.render(context, mouseX, mouseY, delta);
        reloadButton.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        browser.sendMousePress(mouseX(mouseX), mouseY(mouseY), button);
        updateMouseLocation(mouseX, mouseY);
        setFocus();
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        browser.sendMouseRelease(mouseX(mouseX), mouseY(mouseY), button);
        updateMouseLocation(mouseX, mouseY);
        setFocus();
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        browser.sendMouseMove(mouseX(mouseX), mouseY(mouseY));
        updateMouseLocation(mouseX, mouseY);
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        updateMouseLocation(mouseX, mouseY);
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        browser.sendMouseWheel(mouseX(mouseX), mouseY(mouseY), delta, 0);
        updateMouseLocation(mouseX, mouseY);
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        browser.sendKeyPress(keyCode, scanCode, modifiers);
        setFocus();
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        browser.sendKeyRelease(keyCode, scanCode, modifiers);
        setFocus();
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (codePoint == (char) 0) return false;
        browser.sendKeyTyped(codePoint, modifiers);
        setFocus();
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void tick(){
        if(currentUrl != browser.getURL()){
            currentUrl = browser.getURL();
            if(!urlBox.isFocused()) {
                urlBox.setText(Text.of(currentUrl).getString());
                urlBox.setCursorToStart();
            }
        }

        forwardButton.active = browser.canGoForward();
        backButton.active = browser.canGoBack();

        if(browser.isLoading()){
            reloadButton.setMessage(Text.of("\u274C"));
        } else {
            reloadButton.setMessage(Text.of("\u27F3"));
        }
    }

    private void updateMouseLocation(double mouseX, double mouseY){
        lastMouseX = mouseX;
        lastMouseY = mouseY;
    }

    public void setFocus(){
        if(isOverWidgets()){
            browser.setFocus(false);
            urlBox.setFocused(urlBox.isMouseOver(lastMouseX, lastMouseY));
        }else{
            unfocusAllWidgets();
            browser.setFocus(true);
        }
    }
    private boolean isOverWidgets(){
        return urlBox.isMouseOver(lastMouseX, lastMouseY);
    }

    private void unfocusAllWidgets(){
        urlBox.setFocused(false);
    }

    private void reloadAllChildren(){
        if(!children().contains(urlBox)){
            addSelectableChild(urlBox);
        }
        if(!children().contains(forwardButton)){
            addSelectableChild(forwardButton);
        }
        if(!children().contains(backButton)){
            addSelectableChild(backButton);
        }
        if(!children().contains(reloadButton)){
            addSelectableChild(reloadButton);
        }
    }
}
