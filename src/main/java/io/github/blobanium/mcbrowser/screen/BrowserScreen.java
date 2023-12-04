package io.github.blobanium.mcbrowser.screen;

import io.github.blobanium.mcbrowser.MCBrowser;
import io.github.blobanium.mcbrowser.feature.specialbutton.*;
import io.github.blobanium.mcbrowser.util.*;
import io.github.blobanium.mcbrowser.util.button.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import static io.github.blobanium.mcbrowser.MCBrowser.*;


public class BrowserScreen extends Screen {
    public static final int BROWSER_DRAW_OFFSET = 50;

    //Ui
    public TextFieldWidget urlBox;
    public ButtonWidget forwardButton;
    public ButtonWidget backButton;
    public ReloadButton reloadButton;
    private ButtonWidget homeButton;
    private PressableWidget[] navigationButtons;
    private ClickableWidget[] uiElements;
    public ArrayList<TabButton> tabButtons = new ArrayList<>();
    private NewTabButton newTabButton = null;

    public ButtonWidget specialButton;

    private ButtonWidget openInBrowserButton;

    public BrowserScreen(Text title) {
        super(title);

    }

    public void initTabs() {
        for (TabHolder tab : tabs) {
            int index = tabs.indexOf(tab);
            TabButton tabButton = new TabButton(BROWSER_DRAW_OFFSET, BROWSER_DRAW_OFFSET - 40, 100, 15, index);
            tabButtons.add(tabButton);
        }
        for (TabButton tabButton : tabButtons) {
            addSelectableChild(tabButton);
        }
    }

    public void removeTab(int index) {
        remove(tabButtons.get(index));
        tabButtons.get(index).resetIco();
        tabButtons.remove(index);
        updateTabButtonsIndexes(index);
        updateTabSize();
    }

    public void addTab(int index) {
        TabButton tabButton = new TabButton(BROWSER_DRAW_OFFSET, BROWSER_DRAW_OFFSET - 40, 100, 15, index);
        tabButtons.add(index, tabButton);
        updateTabButtonsIndexes(index + 1);
        addSelectableChild(tabButton);
        updateTabSize();
    }

    private void updateTabButtonsIndexes(int i) {
        while (i < tabButtons.size()) {
            tabButtons.get(i).setTab(i);
            i++;
        }
    }

    private void updateTabSize() {
        if (!tabButtons.isEmpty()) {
            int size = Math.min(100, (this.width - (BROWSER_DRAW_OFFSET * 2) - 15) / tabButtons.size() - 5);
            for (TabButton tabButton : tabButtons) {
                tabButton.setWidth(Math.max(15, size));
            }
        }
    }

    @Override
    protected void init() {
        super.init();
        BrowserScreenHelper.instance = this;
        BrowserScreenHelper.tooltipText = null;

        newTabButton = new NewTabButton(BROWSER_DRAW_OFFSET, BROWSER_DRAW_OFFSET - 40, 15, 15, Text.of("+"));
        initTabs();
        updateTabSize();

        urlBox = BrowserScreenHelper.initUrlBox(BROWSER_DRAW_OFFSET, width);

        backButton = BrowserScreenHelper.initButton(Text.of("\u25C0"), button -> getCurrentTab().goBack(), BROWSER_DRAW_OFFSET, BROWSER_DRAW_OFFSET);
        forwardButton = BrowserScreenHelper.initButton(Text.of("\u25B6"), button -> getCurrentTab().goForward(), BROWSER_DRAW_OFFSET + 20, BROWSER_DRAW_OFFSET);
        reloadButton = new ReloadButton(BROWSER_DRAW_OFFSET + 40, BROWSER_DRAW_OFFSET - 20, 15, 15);
        homeButton = BrowserScreenHelper.initButton(Text.of("\u2302"), button -> BrowserScreenHelper.homeButtonAction(), BROWSER_DRAW_OFFSET + 60, BROWSER_DRAW_OFFSET);
        specialButton = ButtonWidget.builder(Text.of(""), button -> SpecialButtonHelper.onPress(getCurrentUrl())).dimensions(BROWSER_DRAW_OFFSET, height - BROWSER_DRAW_OFFSET + 5, 150, 15).build();
        openInBrowserButton = ButtonWidget.builder(Text.of("Open In External Browser"), button -> BrowserScreenHelper.openInBrowser()).dimensions(width - 200, height - BROWSER_DRAW_OFFSET + 5, 150, 15).build();

        navigationButtons = new PressableWidget[]{forwardButton, backButton, reloadButton, homeButton};
        uiElements = new ClickableWidget[]{forwardButton, backButton, reloadButton, homeButton, urlBox, specialButton, openInBrowserButton, newTabButton};
        for (ClickableWidget widget : uiElements) {
            addSelectableChild(widget);
        }
        updateWidgets();
    }

    public void updateWidgets() {
        urlBox.setText(getCurrentTab().getURL());
        backButton.active = getCurrentTab().canGoBack();
        forwardButton.active = getCurrentTab().canGoForward();
        reloadButton.setMessage(Text.of(getCurrentTab().isLoading() ? "\u274C" : "\u27F3"));
        SpecialButtonActions action = SpecialButtonActions.getFromUrlConstantValue(getCurrentUrl());
        if (action != null) {
            specialButton.setMessage(action.getButtonText());
        }
        getCurrentTab().resize(BrowserScreenHelper.scaleX(width, BROWSER_DRAW_OFFSET), BrowserScreenHelper.scaleY(height, BROWSER_DRAW_OFFSET));
    }

    @Override
    public void resize(MinecraftClient minecraft, int i, int j) {
        ArrayList<TabButton> tempList = new ArrayList<>(tabButtons);
        tabButtons.clear();
        super.resize(minecraft, i, j);
        resizeBrowser();
        updateWidgets();
        for (TabButton tabButton : tabButtons) {
            remove(tabButton);
        }
        tabButtons = tempList;
        for (TabButton tabButton : tabButtons) {
            addSelectableChild(tabButton);
        }
        updateTabSize();

        for (ClickableWidget widget : uiElements) {
            if (!children().contains(widget)) {
                addSelectableChild(widget);
            }
        }
    }

    @Override
    public void close() {
        BrowserScreenHelper.instance = null;
        for (TabButton tabButton : tabButtons) {
            tabButton.resetIco();
        }
        super.close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (getCurrentTabHolder().isInit()) {
            getCurrentTab().render(BROWSER_DRAW_OFFSET, BROWSER_DRAW_OFFSET, this.width - BROWSER_DRAW_OFFSET * 2, this.height - BROWSER_DRAW_OFFSET * 2);
        } else {
            getCurrentTabHolder().init();
            resizeBrowser();
        }
        renderButtons(context, mouseX, mouseY, delta);
        if (BrowserScreenHelper.tooltipText != null && BrowserScreenHelper.tooltipText.getBytes().length != 0) {
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

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (MCBrowser.getConfig().asyncBrowserInput) {
            CompletableFuture.runAsync(() -> getCurrentTab().sendMouseMove(BrowserScreenHelper.mouseX(mouseX, BROWSER_DRAW_OFFSET), BrowserScreenHelper.mouseY(mouseY, BROWSER_DRAW_OFFSET)));
        } else {
            getCurrentTab().sendMouseMove(BrowserScreenHelper.mouseX(mouseX, BROWSER_DRAW_OFFSET), BrowserScreenHelper.mouseY(mouseY, BROWSER_DRAW_OFFSET));
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
        if (MCBrowser.getConfig().asyncBrowserInput) {
            CompletableFuture.runAsync(() -> getCurrentTab().sendMouseWheel(BrowserScreenHelper.mouseX(mouseX, BROWSER_DRAW_OFFSET), BrowserScreenHelper.mouseY(mouseY, BROWSER_DRAW_OFFSET), delta, 0));
        } else {
            getCurrentTab().sendMouseWheel(BrowserScreenHelper.mouseX(mouseX, BROWSER_DRAW_OFFSET), BrowserScreenHelper.mouseY(mouseY, BROWSER_DRAW_OFFSET), delta, 0);
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (Screen.hasControlDown() && (keyCode == GLFW.GLFW_KEY_TAB || keyCode == GLFW.GLFW_KEY_T)) {
            final int CTRL_T = GLFW.GLFW_MOD_CONTROL + GLFW.GLFW_KEY_T;
            final int CTRL_SHIFT_T = GLFW.GLFW_MOD_CONTROL + GLFW.GLFW_MOD_SHIFT + GLFW.GLFW_KEY_T;
            final int CTRL_TAB = GLFW.GLFW_MOD_CONTROL + GLFW.GLFW_KEY_TAB;
            final int CTRL_SHIFT_TAB = GLFW.GLFW_MOD_CONTROL + GLFW.GLFW_MOD_SHIFT + GLFW.GLFW_KEY_TAB;


            //TODO: Convert to switch once Code Climate fixes an analysis bug with switches.
            if(keyCode+modifiers == CTRL_T){
                openNewTab();
            }else if(keyCode+modifiers == CTRL_SHIFT_T && !closedTabs.isEmpty()){
                int lastTab = closedTabs.size() - 1;
                openNewTab(closedTabs.get(lastTab));
                closedTabs.remove(lastTab);
            }else if(keyCode+modifiers == CTRL_TAB){
                if (activeTab == tabs.size() - 1) {
                    setActiveTab(0);
                } else {
                    setActiveTab(activeTab + 1);
                }
            }else if(keyCode+modifiers == CTRL_SHIFT_TAB){
                if (activeTab == 0) {
                    setActiveTab(tabs.size() - 1);
                } else {
                    setActiveTab(activeTab - 1);
                }
            }

            setFocus();
            return true;
        }
        sendKeyActivityAndSetFocus(keyCode, scanCode, modifiers, true);

        // Make sure screen isn't sending the enter key if the buttons aren't focused.
        if (!isButtonsFocused() && keyCode == GLFW.GLFW_KEY_ENTER) {
            return true;
        }

        if (keyCode == 256 && this.shouldCloseOnEsc()) { //Removed tab selection functional
            this.close();
            return true;
        } else {
            return this.getFocused() != null && this.getFocused().keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        sendKeyActivityAndSetFocus(keyCode, scanCode, modifiers, false);
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    private void sendKeyActivityAndSetFocus(int keyCode, int scanCode, int modifiers, boolean isPress){
        if (getFlag(keyCode, isPress)) {
            if (MCBrowser.getConfig().asyncBrowserInput) {
                CompletableFuture.runAsync(() -> sendKeyPressRelease(keyCode, scanCode, modifiers, isPress));
            } else {
                sendKeyPressRelease(keyCode, scanCode, modifiers, isPress);
            }
        }
        setFocus();
    }

    private void sendKeyPressRelease(int keyCode, int scanCode, int modifiers, boolean isPress){
        if(isPress){
            getCurrentTab().sendKeyPress(keyCode, scanCode, modifiers);
        }else{
            getCurrentTab().sendKeyRelease(keyCode, scanCode, modifiers);
        }
    }

    private boolean getFlag(int keyCode, boolean isPress){
        if(isPress){
            return !urlBox.isFocused();
        }else{
            return !Screen.hasControlDown() || keyCode != GLFW.GLFW_KEY_TAB;
        }
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (codePoint == (char) 0) return false;
        if (MCBrowser.getConfig().asyncBrowserInput) {
            CompletableFuture.runAsync(() -> getCurrentTab().sendKeyTyped(codePoint, modifiers));
        } else {
            getCurrentTab().sendKeyTyped(codePoint, modifiers);
        }
        setFocus();
        return super.charTyped(codePoint, modifiers);
    }


    //Multi Override Util Methods
    public void setFocus() {
        boolean browserFocus = true;
        for (ClickableWidget widget : uiElements) {
            boolean mouseOver = widget.isMouseOver(BrowserScreenHelper.lastMouseX, BrowserScreenHelper.lastMouseY);
            widget.setFocused(mouseOver);
            if (mouseOver) {
                browserFocus = false;
            }
        }
        getCurrentTab().setFocus(browserFocus);
    }

    private void resizeBrowser() {
        if (width > 100 && height > 100) {
            for (TabHolder tab : tabs) {
                tab.getBrowser().resize(BrowserScreenHelper.scaleX(width, BROWSER_DRAW_OFFSET), BrowserScreenHelper.scaleY(height, BROWSER_DRAW_OFFSET));
            }
        }
        if (this.urlBox != null) {
            urlBox.setWidth(BrowserScreenHelper.getUrlBoxWidth(width, BROWSER_DRAW_OFFSET));
        }

        if (this.specialButton != null) {
            specialButton.setPosition(BROWSER_DRAW_OFFSET, height - BROWSER_DRAW_OFFSET + 5);
        }

        if (this.openInBrowserButton != null) {
            openInBrowserButton.setPosition(width - 200, height - BROWSER_DRAW_OFFSET + 5);
        }
    }

    private void mouseButtonControl(double mouseX, double mouseY, int button, boolean isClick) {
        if (mouseX > BROWSER_DRAW_OFFSET && mouseX < this.width - BROWSER_DRAW_OFFSET && mouseY > BROWSER_DRAW_OFFSET && mouseY < this.height - BROWSER_DRAW_OFFSET) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_4 && getCurrentTab().canGoBack() && !isClick) {
                getCurrentTab().goBack();
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_5 && getCurrentTab().canGoForward() && !isClick) {
                getCurrentTab().goForward();
            } else {
                if (MCBrowser.getConfig().asyncBrowserInput) {
                    CompletableFuture.runAsync(() -> sendMousePressOrRelease(mouseX, mouseY, button, isClick));
                } else {
                    sendMousePressOrRelease(mouseX, mouseY, button, isClick);
                }
            }
        }
        setFocus();
    }

    private void sendMousePressOrRelease(double mouseX, double mouseY, int button, boolean isClick) {
        if (isClick) {
            getCurrentTab().sendMousePress(BrowserScreenHelper.mouseX(mouseX, BROWSER_DRAW_OFFSET), BrowserScreenHelper.mouseY(mouseY, BROWSER_DRAW_OFFSET), button);
        } else {
            getCurrentTab().sendMouseRelease(BrowserScreenHelper.mouseX(mouseX, BROWSER_DRAW_OFFSET), BrowserScreenHelper.mouseY(mouseY, BROWSER_DRAW_OFFSET), button);
        }
    }

    private boolean isButtonsFocused() {
        for (ClickableWidget widget : uiElements) {
            if (widget.isFocused()) {
                return true;
            }
        }
        return false;
    }

    //Rendering Override

    private void renderButtons(DrawContext context, int mouseX, int mouseY, float delta){
        urlBox.renderButton(context, mouseX, mouseY, delta);
        for (PressableWidget button : navigationButtons) {
            button.render(context, mouseX, mouseY, delta);
        }
        if (SpecialButtonHelper.isOnCompatableSite(getCurrentUrl())) {
            specialButton.render(context, mouseX, mouseY, delta);
        }
        for (TabButton tabButton : tabButtons) {
            tabButton.render(context, mouseX, mouseY, delta);
        }
        newTabButton.render(context, mouseX, mouseY, delta);
        openInBrowserButton.render(context, mouseX, mouseY, delta);
    }
}

