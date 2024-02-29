package io.github.blobanium.mcbrowser.screen;

import io.github.blobanium.mcbrowser.MCBrowser;
import io.github.blobanium.mcbrowser.feature.specialbutton.*;
import io.github.blobanium.mcbrowser.util.*;
import io.github.blobanium.mcbrowser.util.button.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public class BrowserScreen extends Screen {
    public static final int BD_OFFSET = 50;
    //Previously BROWSER_DRAW_OFFSET

    //Ui
    public TextFieldWidget urlBox;
    public ButtonWidget forwardButton;
    public ButtonWidget backButton;
    public ReloadButton reloadButton;
    private PressableWidget[] navigationButtons;
    private ClickableWidget[] uiElements;
    public ArrayList<TabButton> tabButtons = new ArrayList<>();
    private NewTabButton newTabButton = null;

    public ButtonWidget specialButton;

    private ButtonWidget openInBrowserButton;

    private int previousLimit;
    private boolean isFpsLowered = false;

    private BrowserImpl currentTab = TabManager.getCurrentTab();

    public BrowserScreen(Text title) {
        super(title);

    }

    public void initTabs() {
        for (TabHolder tab : TabManager.tabs) {
            int index = TabManager.tabs.indexOf(tab);
            TabButton tabButton = new TabButton(BD_OFFSET, BD_OFFSET - 40, 100, 15, index);
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
        TabButton tabButton = new TabButton(BD_OFFSET, BD_OFFSET - 40, 100, 15, index);
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
            int size = Math.min(100, (this.width - (BD_OFFSET * 2) - 15) / tabButtons.size() - 5);
            for (TabButton tabButton : tabButtons) {
                tabButton.setWidth(Math.max(15, size));
            }
        }
    }

    @Override
    protected void init() {
        super.init();

        Window window = MinecraftClient.getInstance().getWindow();
        if(MCBrowser.getConfig().limitBrowserFramerate && window.getFramerateLimit() > MCBrowser.getConfig().browserFPS){
                previousLimit = window.getFramerateLimit();
                window.setFramerateLimit(MCBrowser.getConfig().browserFPS);
                isFpsLowered = true;
        }

        BrowserUtil.instance = this;
        BrowserUtil.tooltipText = null;

        newTabButton = new NewTabButton(BD_OFFSET, BD_OFFSET - 40, 15, 15, Text.of("+"));
        initTabs();
        updateTabSize();

        urlBox = BrowserUtil.initUrlBox(BD_OFFSET, width);

        backButton = BrowserUtil.initButton(Text.of("◀"), button -> currentTab.goBack(), BD_OFFSET, BD_OFFSET);
        forwardButton = BrowserUtil.initButton(Text.of("▶"), button -> currentTab.goForward(), BD_OFFSET + 20, BD_OFFSET);
        reloadButton = new ReloadButton(BD_OFFSET + 40, BD_OFFSET - 20, 15, 15);
        ButtonWidget homeButton = BrowserUtil.initButton(Text.of("⌂"), button -> BrowserUtil.homeButtonAction(), BD_OFFSET + 60, BD_OFFSET);
        specialButton = ButtonWidget.builder(Text.of(""), button -> SpecialButtonHelper.onPress(TabManager.getCurrentUrl())).dimensions(BD_OFFSET, height - BD_OFFSET + 5, 150, 15).build();
        openInBrowserButton = ButtonWidget.builder(Text.of("Open In External Browser"), button -> BrowserUtil.openInBrowser()).dimensions(width - 200, height - BD_OFFSET + 5, 150, 15).build();

        navigationButtons = new PressableWidget[]{forwardButton, backButton, reloadButton, homeButton};
        uiElements = new ClickableWidget[]{forwardButton, backButton, reloadButton, homeButton, urlBox, specialButton, openInBrowserButton, newTabButton};
        for (ClickableWidget widget : uiElements) {
            addSelectableChild(widget);
        }
        updateWidgets();
    }

    public void updateWidgets() {
        urlBox.setText(currentTab.getURL());
        urlBox.setCursorToStart();
        backButton.active = currentTab.canGoBack();
        forwardButton.active = currentTab.canGoForward();
        reloadButton.setMessage(Text.of(currentTab.isLoading() ? "❌" : "⟳"));
        SpecialButtonActions action = SpecialButtonActions.getFromUrlConstantValue(TabManager.getCurrentUrl());
        if (action != null) {
            specialButton.setMessage(action.getButtonText());
        }
        currentTab.resize(BrowserUtil.scaleX(width, BD_OFFSET), BrowserUtil.scaleY(height, BD_OFFSET));
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
        BrowserUtil.instance = null;
        for (TabButton tabButton : tabButtons) {
            tabButton.resetIco();
        }
        if(isFpsLowered){
            MinecraftClient.getInstance().getWindow().setFramerateLimit(previousLimit);
        }
        super.close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (TabManager.getCurrentTabHolder().isInit()) {
            currentTab.render(BD_OFFSET, BD_OFFSET, this.width - BD_OFFSET * 2, this.height - BD_OFFSET * 2);
        } else {
            TabManager.getCurrentTabHolder().init();
            resizeBrowser();
        }
        renderButtons(context, mouseX, mouseY, delta);
        if (BrowserUtil.tooltipText != null && BrowserUtil.tooltipText.getBytes().length != 0) {
            setTooltip(Text.of(BrowserUtil.tooltipText));
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
        BrowserUtil.runAsyncIfEnabled(() -> currentTab.sendMouseMove(BrowserUtil.mouseX(mouseX, BD_OFFSET), BrowserUtil.mouseY(mouseY, BD_OFFSET)));
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        BrowserUtil.updateMouseLocation(mouseX, mouseY);
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        BrowserUtil.runAsyncIfEnabled(() -> currentTab.sendMouseWheel(BrowserUtil.mouseX(mouseX, BD_OFFSET), BrowserUtil.mouseY(mouseY, BD_OFFSET), delta, 0));
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (Screen.hasControlDown() && (keyCode == GLFW.GLFW_KEY_TAB || keyCode == GLFW.GLFW_KEY_T)) {

            TabManager.tabControl(keyCode+modifiers);

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
            BrowserUtil.runAsyncIfEnabled(() -> currentTab.sendKeyPressRelease(keyCode, scanCode, modifiers, isPress));
        }
        setFocus();
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
        BrowserUtil.runAsyncIfEnabled(() -> currentTab.sendKeyTyped(codePoint, modifiers));
        setFocus();
        return super.charTyped(codePoint, modifiers);
    }


    //Multi Override Util Methods
    public void setFocus() {
        boolean browserFocus = true;
        for (ClickableWidget widget : uiElements) {
            boolean mouseOver = widget.isMouseOver(BrowserUtil.lastMouseX, BrowserUtil.lastMouseY);
            widget.setFocused(mouseOver);
            if (mouseOver) {
                browserFocus = false;
            }
        }
        currentTab.setFocus(browserFocus);
    }

    private void resizeBrowser() {
        if (width > 100 && height > 100) {
            for (TabHolder tab : TabManager.tabs) {
                tab.getBrowser().resize(BrowserUtil.scaleX(width, BD_OFFSET), BrowserUtil.scaleY(height, BD_OFFSET));
            }
        }
        if (this.urlBox != null) {
            urlBox.setWidth(BrowserUtil.getUrlBoxWidth(width, BD_OFFSET));
        }

        if (this.specialButton != null) {
            specialButton.setPosition(BD_OFFSET, height - BD_OFFSET + 5);
        }

        if (this.openInBrowserButton != null) {
            openInBrowserButton.setPosition(width - 200, height - BD_OFFSET + 5);
        }
    }

    private void mouseButtonControl(double mouseX, double mouseY, int button, boolean isClick) {
        if (mouseX > BD_OFFSET && mouseX < this.width - BD_OFFSET && mouseY > BD_OFFSET && mouseY < this.height - BD_OFFSET) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_4 && currentTab.canGoBack() && !isClick) {
                currentTab.goBack();
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_5 && currentTab.canGoForward() && !isClick) {
                currentTab.goForward();
            } else {
                BrowserUtil.runAsyncIfEnabled(() -> currentTab.sendMousePressRelease(mouseX, mouseY, button, isClick));
            }
        }
        setFocus();
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
        if (SpecialButtonHelper.isOnCompatableSite(TabManager.getCurrentUrl())) {
            specialButton.render(context, mouseX, mouseY, delta);
        }
        for (TabButton tabButton : tabButtons) {
            tabButton.render(context, mouseX, mouseY, delta);
        }
        newTabButton.render(context, mouseX, mouseY, delta);
        openInBrowserButton.render(context, mouseX, mouseY, delta);
    }
}

