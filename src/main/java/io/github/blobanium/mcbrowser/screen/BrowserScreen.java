package io.github.blobanium.mcbrowser.screen;

import io.github.blobanium.mcbrowser.feature.BrowserUtil;
import io.github.blobanium.mcbrowser.MCBrowser;
import io.github.blobanium.mcbrowser.feature.specialbutton.SpecialButtonActions;
import io.github.blobanium.mcbrowser.feature.specialbutton.SpecialButtonHelper;
import io.github.blobanium.mcbrowser.util.BrowserImpl;
import io.github.blobanium.mcbrowser.util.BrowserScreenHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.CompletableFuture;


public class BrowserScreen extends Screen {
    private static final int BROWSER_DRAW_OFFSET = 50;

    private BrowserImpl browser;

    //URL
    private String initURL;

    //Ui
    private TextFieldWidget urlBox;
    public ButtonWidget forwardButton;
    public ButtonWidget backButton;
    public ButtonWidget reloadButton;
    private ButtonWidget homeButton;
    private ButtonWidget[] navigationButtons;
    private ClickableWidget[] uiElements;

    private ButtonWidget specialButton;

    public BrowserScreen(Text title, String url) {
        super(title);
        this.initURL = url;
    }

    @Override
    protected void init() {
        super.init();
        if (browser == null) {
            boolean transparent = false;
            browser = BrowserScreenHelper.createBrowser(this.initURL, transparent);
            resizeBrowser();

            initUrlBox();
            backButton = BrowserScreenHelper.initButton(Text.of("\u25C0"), button -> browser.goBack(), BROWSER_DRAW_OFFSET, BROWSER_DRAW_OFFSET);
            forwardButton = BrowserScreenHelper.initButton(Text.of("\u25B6"), button -> browser.goForward(), BROWSER_DRAW_OFFSET + 20, BROWSER_DRAW_OFFSET);
            reloadButton = BrowserScreenHelper.initButton(Text.of("\u27F3"), button -> { if(browser.isLoading()) {browser.stopLoad();} else {browser.reload();} }, BROWSER_DRAW_OFFSET + 40, BROWSER_DRAW_OFFSET);
            homeButton = BrowserScreenHelper.initButton(Text.of("\u2302"), button -> browser.loadURL(MCBrowser.getConfig().homePage), BROWSER_DRAW_OFFSET + 60, BROWSER_DRAW_OFFSET);
            specialButton = ButtonWidget.builder(Text.of(""), button -> SpecialButtonHelper.onPress(BrowserScreenHelper.currentUrl)).dimensions(BROWSER_DRAW_OFFSET, height - BROWSER_DRAW_OFFSET + 5,  150, 15).build();

            navigationButtons = new ButtonWidget[]{forwardButton, backButton, reloadButton, homeButton};
            uiElements = new ClickableWidget[]{forwardButton, backButton, reloadButton, homeButton, urlBox, specialButton};
            for(ClickableWidget widget : uiElements){
                addSelectableChild(widget);
            }

            BrowserScreenHelper.instance = this;
        }
    }

    private void initUrlBox(){
        this.urlBox = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, BROWSER_DRAW_OFFSET + 80,BROWSER_DRAW_OFFSET-20,BrowserScreenHelper.getUrlBoxWidth(width, BROWSER_DRAW_OFFSET),15, Text.of("")){
            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers){
                if(isFocused()) {
                    browser.setFocus(false);
                    if(keyCode == GLFW.GLFW_KEY_ENTER){
                        browser.loadURL(BrowserUtil.prediffyURL(getText()));
                        setFocused(false);
                    }
                }
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
        };
        urlBox.setMaxLength(2048); //Most browsers have a max length of 2048
    }

    private void resizeBrowser() {
        if (width > 100 && height > 100) {
            browser.resize(BrowserScreenHelper.scaleX(width, BROWSER_DRAW_OFFSET), BrowserScreenHelper.scaleY(height, BROWSER_DRAW_OFFSET));
        }
        if(this.urlBox != null) {
            urlBox.setWidth(BrowserScreenHelper.getUrlBoxWidth(width, BROWSER_DRAW_OFFSET));
        }

        if(this.specialButton != null){
            specialButton.setPosition(BROWSER_DRAW_OFFSET, height - BROWSER_DRAW_OFFSET + 5);
        }
    }

    @Override
    public void resize(MinecraftClient minecraft, int i, int j) {
        super.resize(minecraft, i, j);
        resizeBrowser();

        for(ClickableWidget widget : uiElements){
            if(!children().contains(widget)){
                addSelectableChild(widget);
            }
        }
    }

    @Override
    public void close() {
        BrowserScreenHelper.instance = null;
        browser.close();
        MCBrowser.requestOpen = false;
        super.close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        BrowserScreenHelper.renderBrowser(BROWSER_DRAW_OFFSET, width, height, browser.getRenderer().getTextureID());
        urlBox.renderButton(context, mouseX, mouseY, delta);
        for(ButtonWidget button : navigationButtons){
            button.render(context, mouseX, mouseY, delta);
        }
        if(SpecialButtonHelper.isOnCompatableSite(BrowserScreenHelper.currentUrl)) {
            specialButton.render(context, mouseX, mouseY, delta);
        }
        if(BrowserScreenHelper.tooltipText != null && BrowserScreenHelper.tooltipText.getBytes().length != 0) {
            setTooltip(Text.of(BrowserScreenHelper.tooltipText));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseButtonControl(mouseX, mouseY, button, true);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        mouseButtonControl(mouseX, mouseY, button, false);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void mouseButtonControl(double mouseX, double mouseY, int button, boolean isClick){
        if(MCBrowser.getConfig().asyncBrowserInput) {
            if(isClick){
                CompletableFuture.runAsync(() -> browser.sendMousePress(BrowserScreenHelper.mouseX(mouseX, BROWSER_DRAW_OFFSET), BrowserScreenHelper.mouseY(mouseY, BROWSER_DRAW_OFFSET), button));
            }else {
                CompletableFuture.runAsync(() -> browser.sendMouseRelease(BrowserScreenHelper.mouseX(mouseX, BROWSER_DRAW_OFFSET), BrowserScreenHelper.mouseY(mouseY, BROWSER_DRAW_OFFSET), button));
            }
        }else {
            if(isClick){
                browser.sendMousePress(BrowserScreenHelper.mouseX(mouseX, BROWSER_DRAW_OFFSET), BrowserScreenHelper.mouseY(mouseY, BROWSER_DRAW_OFFSET), button);
            }else {
                browser.sendMouseRelease(BrowserScreenHelper.mouseX(mouseX, BROWSER_DRAW_OFFSET), BrowserScreenHelper.mouseY(mouseY, BROWSER_DRAW_OFFSET), button);
            }
        }
        setFocus();
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if(MCBrowser.getConfig().asyncBrowserInput) {
            CompletableFuture.runAsync(() -> browser.sendMouseMove(BrowserScreenHelper.mouseX(mouseX, BROWSER_DRAW_OFFSET), BrowserScreenHelper.mouseY(mouseY, BROWSER_DRAW_OFFSET)));
        } else{
            browser.sendMouseMove(BrowserScreenHelper.mouseX(mouseX, BROWSER_DRAW_OFFSET), BrowserScreenHelper.mouseY(mouseY, BROWSER_DRAW_OFFSET));
        }
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        BrowserScreenHelper.updateMouseLocation(mouseX, mouseY);
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if(MCBrowser.getConfig().asyncBrowserInput) {
            CompletableFuture.runAsync(() -> browser.sendMouseWheel(BrowserScreenHelper.mouseX(mouseX, BROWSER_DRAW_OFFSET), BrowserScreenHelper.mouseY(mouseY, BROWSER_DRAW_OFFSET), delta, 0));
        } else{
            browser.sendMouseWheel(BrowserScreenHelper.mouseX(mouseX, BROWSER_DRAW_OFFSET), BrowserScreenHelper.mouseY(mouseY, BROWSER_DRAW_OFFSET), delta, 0);
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(!urlBox.isFocused()) {
            if(MCBrowser.getConfig().asyncBrowserInput) {
                CompletableFuture.runAsync(() -> browser.sendKeyPress(keyCode, scanCode, modifiers));
            }else{
                browser.sendKeyPress(keyCode, scanCode, modifiers);
            }
        }

        //Set Focus
        setFocus();

        // Make sure screen isn't sending the enter key if the buttons aren't focused.
        if(!isButtonsFocused() && keyCode == GLFW.GLFW_KEY_ENTER){
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private boolean isButtonsFocused(){
        for(ClickableWidget widget : uiElements){
            if(widget.isFocused()){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if(MCBrowser.getConfig().asyncBrowserInput) {
            CompletableFuture.runAsync(() -> browser.sendKeyRelease(keyCode, scanCode, modifiers));
        } else {
            browser.sendKeyRelease(keyCode, scanCode, modifiers);
        }
        setFocus();
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if(codePoint == (char) 0) return false;
        if(MCBrowser.getConfig().asyncBrowserInput) {
            CompletableFuture.runAsync(() -> browser.sendKeyTyped(codePoint, modifiers));
        } else {
            browser.sendKeyTyped(codePoint, modifiers);
        }
        setFocus();
        return super.charTyped(codePoint, modifiers);
    }

    public void setFocus(){
        if(isOverWidgets()){
            browser.setFocus(false);
            for(ClickableWidget widget : uiElements){
                widget.setFocused(widget.isMouseOver(BrowserScreenHelper.lastMouseX, BrowserScreenHelper.lastMouseY));
            }
        }else{
            for(ClickableWidget widget : uiElements){
                widget.setFocused(false);
            }
            browser.setFocus(true);
        }
    }

    private boolean isOverWidgets(){
        for(ClickableWidget widget : uiElements){
            if(widget.isMouseOver(BrowserScreenHelper.lastMouseX, BrowserScreenHelper.lastMouseY)){
                return true;
            }
        }
        return false;
    }

    public void onUrlChange(){
        if(urlBox.isFocused()) {
            urlBox.setFocused(false);
        }
        urlBox.setText(Text.of(BrowserScreenHelper.currentUrl).getString());
        urlBox.setCursorToStart();
        SpecialButtonActions action = SpecialButtonActions.getFromUrlConstantValue(BrowserScreenHelper.currentUrl);
        if(action != null) {
            specialButton.setMessage(action.getButtonText());
        }
    }
}

