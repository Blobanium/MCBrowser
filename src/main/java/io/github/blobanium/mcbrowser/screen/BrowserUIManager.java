package io.github.blobanium.mcbrowser.screen;

import io.github.blobanium.mcbrowser.feature.specialbutton.SpecialButtonHelper;
import io.github.blobanium.mcbrowser.util.BrowserImpl;
import io.github.blobanium.mcbrowser.util.BrowserUtil;
import io.github.blobanium.mcbrowser.util.TabManager;
import io.github.blobanium.mcbrowser.util.button.NewTabButton;
import io.github.blobanium.mcbrowser.util.button.ReloadButton;
import io.github.blobanium.mcbrowser.util.button.TabButton;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;

import java.util.Arrays;

public class BrowserUIManager {
    private final BrowserScreen screen;
    private final int screenWidth;
    private final int screenHeight;

    // Navigation elements
    private ButtonWidget forwardButton;
    private ButtonWidget backButton;
    private ReloadButton reloadButton;
    private ButtonWidget homeButton;
    private PressableWidget[] navigationButtons;

    // URL and special buttons
    private TextFieldWidget urlBox;
    private ButtonWidget specialButton;
    private ButtonWidget openInBrowserButton;

    // Zoom controls
    private ButtonWidget zoomInButton;
    private ButtonWidget zoomOutButton;
    private TextWidget zoomDetails;
    private ClickableWidget[] zoomElements;

    // New tab button
    private NewTabButton newTabButton;

    // All UI elements for focus management
    private ClickableWidget[] uiElements;

    public BrowserUIManager(BrowserScreen screen, int width, int height) {
        this.screen = screen;
        this.screenWidth = width;
        this.screenHeight = height;
    }

    public void initializeAllWidgets() {
        initializeNavigationWidgets();
        initializeUrlBox();
        initializeSpecialButtons();
        initializeZoomWidgets();
        initializeNewTabButton();
        assembleUIElementsArray();
    }

    private void initializeNavigationWidgets() {
        backButton = BrowserUtil.initButton(Text.of("◀"), button -> screen.currentTab.goBack(), BrowserScreen.BD_OFFSET, 1);
        forwardButton = BrowserUtil.initButton(Text.of("▶"), button -> screen.currentTab.goForward(), BrowserScreen.BD_OFFSET + 20, 1);
        reloadButton = new ReloadButton(BrowserScreen.BD_OFFSET + 40, BrowserScreen.BD_OFFSET - 20, 15, 15);
        homeButton = BrowserUtil.initButton(Text.of("⌂"), button -> BrowserUtil.homeButtonAction(), BrowserScreen.BD_OFFSET + 60, 1);
        navigationButtons = new PressableWidget[]{forwardButton, backButton, reloadButton, homeButton};
    }

    private void initializeUrlBox() {
        urlBox = BrowserUtil.initUrlBox(BrowserScreen.BD_OFFSET, screenWidth);
    }

    private void initializeSpecialButtons() {
        specialButton = ButtonWidget.builder(Text.of(""), button -> SpecialButtonHelper.onPress(TabManager.getCurrentUrl()))
                .dimensions(BrowserScreen.BD_OFFSET, screenHeight - BrowserScreen.BD_OFFSET + 5, 150, 15)
                .build();
        openInBrowserButton = ButtonWidget.builder(Text.of("Open In External Browser"), button -> BrowserUtil.openInBrowser())
                .dimensions(screenWidth - 200, screenHeight - BrowserScreen.BD_OFFSET + 5, 150, 15)
                .build();
    }

    private void initializeZoomWidgets() {
        zoomDetails = new TextWidget(BrowserUtil.getZoomLevelText(screen.currentTab.getZoomLevel()), MinecraftClient.getInstance().textRenderer);
        zoomDetails.setPosition(screenWidth - 50 - zoomDetails.getWidth(), BrowserScreen.BD_OFFSET - 49);
        zoomInButton = BrowserUtil.initButton(Text.of("+"), button -> screen.zoomControl(BrowserUtil.ZoomActions.INCREASE), screenWidth - 65, 2);
        zoomOutButton = BrowserUtil.initButton(Text.of("-"), button -> screen.zoomControl(BrowserUtil.ZoomActions.DECREASE), screenWidth - 85, 2);
        zoomElements = new ClickableWidget[]{zoomInButton, zoomOutButton, zoomDetails};
    }

    private void initializeNewTabButton() {
        newTabButton = new NewTabButton(BrowserScreen.BD_OFFSET, BrowserScreen.BD_OFFSET - 40, 15, 15, Text.of("+"));
    }

    private void assembleUIElementsArray() {
        uiElements = new ClickableWidget[]{forwardButton, backButton, reloadButton, homeButton, urlBox, specialButton, openInBrowserButton, newTabButton, zoomDetails, zoomInButton, zoomOutButton};
    }

    public void registerWidgetsAsChildren(BrowserScreen screen) {
        for (ClickableWidget widget : uiElements) {
            screen.addWidget(widget);
        }
    }

    public void reregisterWidgetsAsChildren(BrowserScreen screen) {
        for (ClickableWidget widget : uiElements) {
            if (!screen.children().contains(widget)) {
                screen.addWidget(widget);
            }
        }
    }

    public void updateNavigationButtons(BrowserImpl currentTab) {
        backButton.active = currentTab.canGoBack();
        forwardButton.active = currentTab.canGoForward();
        reloadButton.setMessage(Text.of(currentTab.isLoading() ? "❌" : "⟳"));
    }

    public void updateZoomDisplay(double zoomLevel) {
        zoomDetails.setMessage(BrowserUtil.getZoomLevelText(zoomLevel));
    }

    public void renderAllWidgets(DrawContext context, int mouseX, int mouseY, float delta) {
        urlBox.renderWidget(context, mouseX, mouseY, delta);
        for (PressableWidget button : navigationButtons) {
            button.render(context, mouseX, mouseY, delta);
        }
        if (SpecialButtonHelper.isOnCompatableSite(TabManager.getCurrentUrl())) {
            specialButton.render(context, mouseX, mouseY, delta);
        }
        for (TabButton tabButton : screen.tabButtons) {
            tabButton.render(context, mouseX, mouseY, delta);
        }
        for (ClickableWidget zoom : zoomElements) {
            if (BrowserUtil.ZoomActions.shouldRenderZoomElements()) {
                if (zoom.isMouseOver(mouseX, mouseY)) {
                    BrowserUtil.ZoomActions.resetLastTimeCalled();
                }
                zoom.active = true;
                zoom.render(context, mouseX, mouseY, delta);
            } else {
                zoom.active = false;
            }
        }
        newTabButton.render(context, mouseX, mouseY, delta);
        openInBrowserButton.render(context, mouseX, mouseY, delta);
    }

    public void resizeWidgets(int width, int height) {
        if (urlBox != null) {
            urlBox.setWidth(BrowserUtil.getUrlBoxWidth(width, BrowserScreen.BD_OFFSET));
        }
        if (specialButton != null) {
            specialButton.setPosition(BrowserScreen.BD_OFFSET, height - BrowserScreen.BD_OFFSET + 5);
        }
        if (openInBrowserButton != null) {
            openInBrowserButton.setPosition(width - 200, height - BrowserScreen.BD_OFFSET + 5);
        }
    }

    public boolean isAnyUIElementFocused() {
        return Arrays.stream(uiElements).anyMatch(ClickableWidget::isFocused);
    }

    public ClickableWidget[] getAllUIElements() {
        return uiElements;
    }

    public TextFieldWidget getUrlBox() {
        return urlBox;
    }

    public ButtonWidget getSpecialButton() {
        return specialButton;
    }

    public ButtonWidget getOpenInBrowserButton() {
        return openInBrowserButton;
    }
}