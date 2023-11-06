package io.github.blobanium.mcbrowser.feature.specialbutton;


public class SpecialButtonHelper {

    public static void onPress(String url){
        try {
            if(SpecialButtonActions.getFromUrlConstantValue(url) != null) {
                SpecialButtonActions.getFromUrlConstantValue(url).getOnExecute().run();
            }
        }catch (IllegalArgumentException e){
            e.printStackTrace();
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
