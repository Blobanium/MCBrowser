package io.github.blobanium.mcbrowser.util.button;

import io.github.blobanium.mcbrowser.screen.BrowserScreen;
import io.github.blobanium.mcbrowser.util.BrowserScreenHelper;
import io.github.blobanium.mcbrowser.util.TabManager;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.text.Text;

public class NewTabButton extends PressableWidget {
    int startX;

    public NewTabButton(int startX, int y, int width, int height, Text text) {
        super(0, y, width, height, text);
        this.startX = startX;
    }

    @Override
    public int getX() {
        return Math.min(BrowserScreenHelper.instance.width - BrowserScreen.BROWSER_DRAW_OFFSET - 15, startX + ((BrowserScreenHelper.instance.tabButtons.size()) * 105));
    }

    @Override
    public void onPress() {
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
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
