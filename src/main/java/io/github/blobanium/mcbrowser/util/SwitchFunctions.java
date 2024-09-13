package io.github.blobanium.mcbrowser.util;

import io.github.blobanium.mcbrowser.feature.specialbutton.SpecialButtonAction;
import io.github.blobanium.mcbrowser.feature.specialbutton.SpecialButtonActions;
import net.minecraft.MinecraftVersion;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.net.MalformedURLException;
import java.net.URL;

import static io.github.blobanium.mcbrowser.util.BrowserUtil.Keybinds.*;


/**
 * The SwitchFunctions class contains static utility classes and methods.
 * There exists an issue where code climate quality fails to properly rate a file. Making it look like it got a perfect rating, but in reality the code is really sloppy.
 * Code Climate is a third party application blobanium uses to make sure code is as clean as possible.
 * This file serves as a temporary workaround for this issue.
 */
public class SwitchFunctions {


    public static void tabControl(int keyCodeModifiers){
        //TODO: Convert to switch once Code Climate fixes an analysis bug with switches.
        if(keyCodeModifiers == CTRL_T){
            TabManager.openNewTab();
        }else if(keyCodeModifiers == CTRL_SHIFT_T && !TabManager.closedTabs.isEmpty()){
            int lastTab = TabManager.closedTabs.size() - 1;
            TabManager.openNewTab(TabManager.closedTabs.get(lastTab));
            TabManager.closedTabs.remove(lastTab);
        }else if(keyCodeModifiers == CTRL_TAB){
            if (TabManager.activeTab == TabManager.tabs.size() - 1) {
                TabManager.setActiveTab(0);
            } else {
                TabManager.setActiveTab(TabManager.activeTab + 1);
            }
        }else if(keyCodeModifiers == CTRL_SHIFT_TAB){
            if (TabManager.activeTab == 0) {
                TabManager.setActiveTab(TabManager.tabs.size() - 1);
            } else {
                TabManager.setActiveTab(TabManager.activeTab - 1);
            }
        }
    }

    public static class SpecialButtonActionSwitches{
        public static MutableText getTranslation(byte type, SpecialButtonActions action) {
            return switch (action) {
                case MODRINTH_MOD -> switch (type) {
                    case SpecialButtonAction.START_DL_DESCRIPTION -> Text.translatable("mcbrowser.download.toast.started.description.mod");
                    case SpecialButtonAction.END_DL_DESCRIPTION -> Text.translatable("mcbrowser.download.toast.complete.description.mod");
                    default -> throw new IllegalStateException("Unexpected type value: " + type);
                };
                case MODRINTH_RP -> switch (type) {
                    case SpecialButtonAction.START_DL_DESCRIPTION -> Text.translatable("mcbrowser.download.toast.started.description.rp");
                    case SpecialButtonAction.END_DL_DESCRIPTION -> Text.translatable("mcbrowser.download.toast.complete.description.rp");
                    default -> throw new IllegalStateException("Unexpected type value: " + type);
                };

                //Reserved for future usage.
                //noinspection UnnecessaryDefault
                default -> throw new IllegalStateException("Unexpected action value: " + action);
            };
        }

        public static URL getTargetURL(SpecialButtonActions action) throws MalformedURLException {
            return switch (action) {
                case MODRINTH_MOD -> new URL("https://api.modrinth.com/v2/project/" + SpecialButtonAction.getModrinthSlugFromUrl(TabManager.getCurrentUrl()) + "/version?game_versions=[%22" + MinecraftVersion.CURRENT.getName() + "%22]&loaders=[%22fabric%22]");
                case MODRINTH_RP -> new URL("https://api.modrinth.com/v2/project/" + SpecialButtonAction.getModrinthSlugFromUrl(TabManager.getCurrentUrl()) + "/version?game_versions=[%22" + MinecraftVersion.CURRENT.getName() + "%22]");

                //Reserved for future usage.
                //noinspection UnnecessaryDefault
                default -> throw new IllegalStateException("Unexpected action value: " + action);
            };
        }

        public static String getTargetDirectory(SpecialButtonActions action) {
            return switch (action) {
                case MODRINTH_MOD -> "mods/";
                case MODRINTH_RP -> "resourcepacks/";
            };
        }
    }
}
