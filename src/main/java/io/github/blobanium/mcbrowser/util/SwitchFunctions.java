package io.github.blobanium.mcbrowser.util;

import io.github.blobanium.mcbrowser.feature.specialbutton.SpecialButtonAction;
import io.github.blobanium.mcbrowser.feature.specialbutton.SpecialButtonActions;
import net.minecraft.MinecraftVersion;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URI;



/**
 * The SwitchFunctions class contains static utility classes and methods.
 * There exists an issue where code climate quality fails to properly rate a file. Making it look like it got a perfect rating, but in reality the code is really sloppy.
 * Code Climate is a third party application blobanium uses to make sure code is as clean as possible.
 * This file serves as a temporary workaround for this issue.
 *
 * NOTE: as of 10/21/2025 code climate changed to qlty with a lot of changes as this bug has been fixed.
 */

@Deprecated
public class SwitchFunctions {
    public static boolean ctrlKeyPressedSwitch(int keyCode, int modifiers){
        switch (keyCode) {
            case GLFW.GLFW_KEY_TAB:
            case GLFW.GLFW_KEY_T:
                // Tab Functions
                TabManager.tabControl(keyCode + modifiers);
                BrowserUtil.instance.setFocus();
                return true;
            case GLFW.GLFW_KEY_EQUAL:
                BrowserUtil.instance.zoomControl(BrowserUtil.ZoomActions.INCREASE);
                return true;
            case GLFW.GLFW_KEY_MINUS:
                BrowserUtil.instance.zoomControl(BrowserUtil.ZoomActions.DECREASE);
                return true;
            case GLFW.GLFW_KEY_0:
                BrowserUtil.instance.zoomControl(BrowserUtil.ZoomActions.RESET);
                return true;
        }
        return false;
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

        public static URL getTargetURL(SpecialButtonActions action) throws MalformedURLException, URISyntaxException {
            return switch (action) {
                case MODRINTH_MOD -> new URI("https://api.modrinth.com/v2/project/" + SpecialButtonAction.getModrinthSlugFromUrl(TabManager.getCurrentUrl()) + "/version?game_versions=[%22" + MinecraftVersion.create().name() + "%22]&loaders=[%22fabric%22]").toURL();
                case MODRINTH_RP -> new URI("https://api.modrinth.com/v2/project/" + SpecialButtonAction.getModrinthSlugFromUrl(TabManager.getCurrentUrl()) + "/version?game_versions=[%22" + MinecraftVersion.create().name() + "%22]").toURL();

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
