package io.github.blobanium.mcbrowser.feature.specialbutton;

import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public enum SpecialButtonActions {
    MODRINTH_MOD("https://modrinth.com/mod/", Text.of("Download Mod"), SpecialButtonAction::downloadModrinthMod);

    private final String url;
    private final Text buttonText;
    private final Runnable onExecute;


    SpecialButtonActions(String url, Text buttonText, Runnable onExecute){
        this.url = url;
        this.buttonText = buttonText;
        this.onExecute = onExecute;
    }


    public String getUrl() {
        return url;
    }

    public Text getButtonText() {
        return buttonText;
    }

    public Runnable getOnExecute() {
        return onExecute;
    }

    public static List<String> getAllUrls() {
        List<String> urls = new ArrayList<>();
        for (SpecialButtonActions action : values()) {
            urls.add(action.getUrl());
        }
        return urls;
    }

    public static SpecialButtonActions getFromUrlConstantValue(String urlConstantValue) {
        for (SpecialButtonActions action : SpecialButtonActions.values()) {
            if (urlConstantValue.contains(action.getUrl())) {
                return action;
            }
        }
        return null;
    }
}
