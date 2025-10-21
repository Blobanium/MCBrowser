package io.github.blobanium.mcbrowser.feature.specialbutton;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.blobanium.mcbrowser.MCBrowser;
import io.github.blobanium.mcbrowser.util.TabManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.MinecraftVersion;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.concurrent.CompletableFuture;

public class SpecialButtonAction {
    public static final byte START_DL_DESCRIPTION = 0x00;
    public static final byte END_DL_DESCRIPTION = 0x01;

    //These two methods exist for compatability purposes. May change later when i learn more about enum values.
    public static void downloadModrinthMod() {
        downloadModrinth(SpecialButtonActions.MODRINTH_MOD);
    }

    public static void downloadModrinthRP() {
        downloadModrinth(SpecialButtonActions.MODRINTH_RP);
    }

    private static void downloadModrinth(SpecialButtonActions action) {
        MCBrowser.sendToastMessage(Text.translatable("mcbrowser.download.toast.started"), SpecialButtonActionSwitches.getTranslation(START_DL_DESCRIPTION, action));

        CompletableFuture.runAsync(() -> {
            try {
                //Get the file from modrinth's API
                URL url = SpecialButtonActionSwitches.getTargetURL(action);
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setRequestMethod("GET");
                http.connect();
                InputStream stream = http.getInputStream();
                String json = new String(stream.readAllBytes());
                http.disconnect();

                //Analyze JSON Result from Modrinth API
                Gson gson = new Gson();
                JsonArray array = gson.fromJson(json, JsonArray.class);
                JsonObject object = array.get(0).getAsJsonObject();
                JsonArray filesArray = object.get("files").getAsJsonArray();
                JsonObject file = filesArray.get(0).getAsJsonObject();

                //get file
                URL downloadURL = new URI(file.get("url").getAsString()).toURL();
                FileUtils.copyURLToFile(downloadURL, new File(FabricLoader.getInstance().getGameDir().toFile(), SpecialButtonActionSwitches.getTargetDirectory(action) + cleanseFileUrl(downloadURL.getFile())));

                MCBrowser.sendToastMessage(Text.translatable("mcbrowser.download.toast.complete"), SpecialButtonActionSwitches.getTranslation(END_DL_DESCRIPTION, action));
            } catch (IOException | URISyntaxException e) {
                MCBrowser.sendToastMessage(Text.translatable("mcbrowser.download.toast.failed"), Text.translatable("mcbrowser.download.toast.failed.description"));
                MCBrowser.LOGGER.error("Failed to download file", e);
            }
        });
    }


    private static String cleanseFileUrl(String url) {
        String[] array = url.split("/");
        return array[array.length - 1].replace("%2B", "+");
    }

    public static String getModrinthSlugFromUrl(String url) {
        String string = url.replace("https://modrinth.com/", "");
        string = string.substring(string.indexOf("/") + 1);
        return string.split("/")[0];
    }

    public static class SpecialButtonActionSwitches{

        public static MutableText getTranslation(byte type, SpecialButtonActions action) {
            return switch (action) {
                case MODRINTH_MOD -> switch (type) {
                    case START_DL_DESCRIPTION -> Text.translatable("mcbrowser.download.toast.started.description.mod");
                    case END_DL_DESCRIPTION -> Text.translatable("mcbrowser.download.toast.complete.description.mod");
                    default -> throw new IllegalStateException("Unexpected type value: " + type);
                };
                case MODRINTH_RP -> switch (type) {
                    case START_DL_DESCRIPTION -> Text.translatable("mcbrowser.download.toast.started.description.rp");
                    case END_DL_DESCRIPTION -> Text.translatable("mcbrowser.download.toast.complete.description.rp");
                    default -> throw new IllegalStateException("Unexpected type value: " + type);
                };

                //Reserved for future usage.
                //noinspection UnnecessaryDefault
                default -> throw new IllegalStateException("Unexpected action value: " + action);
            };
        }

        public static URL getTargetURL(SpecialButtonActions action) throws MalformedURLException, URISyntaxException {
            return switch (action) {
                case MODRINTH_MOD -> new URI("https://api.modrinth.com/v2/project/" + getModrinthSlugFromUrl(TabManager.getCurrentUrl()) + "/version?game_versions=[%22" + MinecraftVersion.create().name() + "%22]&loaders=[%22fabric%22]").toURL();
                case MODRINTH_RP -> new URI("https://api.modrinth.com/v2/project/" + getModrinthSlugFromUrl(TabManager.getCurrentUrl()) + "/version?game_versions=[%22" + MinecraftVersion.create().name() + "%22]").toURL();

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
