package io.github.blobanium.mcbrowser.util.button;

import io.github.blobanium.mcbrowser.screen.BrowserScreen;
import io.github.blobanium.mcbrowser.util.BrowserUtil;
import io.github.blobanium.mcbrowser.util.TabManager;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.text.Text;

public class NewTabButton extends PressableWidget {
    final int startX;

    public NewTabButton(int startX, int y, int width, int height, Text text) {
        super(0, y, width, height, text);
        this.startX = startX;
    }

    @Override
    public int getX() {
        return Math.min(BrowserUtil.instance.width - BrowserScreen.BD_OFFSET - 15, startX + ((BrowserUtil.instance.tabButtons.size()) * 105));
    }

    @Override
    public void onPress() {
        //Required for Implementation
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        //Required for Implementation
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.clicked(mouseX, mouseY)) {
            if (button == 2) {
                int i = TabManager.activeTab;
                TabManager.openNewTab();
                TabManager.setActiveTab(i);
                return true;
            }
            TabManager.openNewTab();
            return true;
        }
        return false;
    }
}
