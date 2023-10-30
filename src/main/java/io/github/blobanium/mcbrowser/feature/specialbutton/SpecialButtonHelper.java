package io.github.blobanium.mcbrowser.feature.specialbutton;

import java.util.List;

public class SpecialButtonHelper {

    public static void onPress(String url){
        try {
            if(SpecialButtonActions.getFromUrlConstantValue(url) != null) {
                SpecialButtonActions.getFromUrlConstantValue(url).getOnExecute().run();
            }
        }catch (IllegalArgumentException ignored){
            ignored.printStackTrace();
        }
    }

    public static boolean isOnCompatableSite(String url){
        for(String element : SpecialButtonActions.getAllUrls()){
            if(url.contains(element)){
                return true;
            }
        }
        return false;
    }
}
