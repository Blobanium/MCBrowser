package io.github.blobanium.mcbrowser.util.button;

import io.github.blobanium.mcbrowser.screen.BrowserScreen;
import io.github.blobanium.mcbrowser.util.BrowserUtil;
import io.github.blobanium.mcbrowser.util.TabManager;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.input.AbstractInput;
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
    public void onPress(AbstractInput input) {
        //Required for Implementation
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        //Required for Implementation
    }

    @Override
    protected void drawIcon(net.minecraft.client.gui.DrawContext context, int x, int y, float delta) {
        // Draw the default button with the "+" text
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (this.isSelected()){
            if (click.button() == 2) {
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
