package io.github.blobanium.mcbrowser.feature.specialbutton;

import io.github.blobanium.mcbrowser.MCBrowser;

public class SpecialButtonHelper {

    public static void onPress(String url){
        try {
            if(SpecialButtonActions.getFromUrlConstantValue(url) != null) {
                SpecialButtonActions.getFromUrlConstantValue(url).getOnExecute().run();
            }
        }catch (IllegalArgumentException e){
            MCBrowser.LOGGER.error("An error occurred with specialButtons, please report this to the dev", e);
        }
    }

    public static boolean isOnCompatableSite(String url){
        for(String element : SpecialButtonActions.getAllUrls()){
            if(url != null && url.contains(element)){
                return true;
            }
        }
        return false;
    }
}
