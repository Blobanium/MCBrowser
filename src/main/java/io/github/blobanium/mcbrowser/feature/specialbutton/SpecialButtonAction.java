package io.github.blobanium.mcbrowser.feature.specialbutton;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.blobanium.mcbrowser.MCBrowser;
import io.github.blobanium.mcbrowser.util.TabManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.MinecraftVersion;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class SpecialButtonAction {
    private static final byte START_DL_DESCRIPTION = 0x00;
    private static final byte END_DL_DESCRIPTION = 0x01;

    //These two methods exist for compatability purposes. May change later when i learn more about enum values.
    public static void downloadModrinthMod() {
        downloadModrinth(SpecialButtonActions.MODRINTH_MOD);
    }

    public static void downloadModrinthRP() {
        downloadModrinth(SpecialButtonActions.MODRINTH_RP);
    }

    private static void downloadModrinth(SpecialButtonActions action) {
        sendToastMessage(Text.translatable("mcbrowser.download.toast.started"), getTranslation(START_DL_DESCRIPTION, action));

        CompletableFuture.runAsync(() -> {
            try {
                //Get the file from modrinth's API
                URL url = getTargetURL(action);
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
                URL downloadURL = new URL(file.get("url").getAsString());
                FileUtils.copyURLToFile(downloadURL, new File(FabricLoader.getInstance().getGameDir().toFile(), getTargetDirectory(action) + cleanseFileUrl(downloadURL.getFile())));

                sendToastMessage(Text.translatable("mcbrowser.download.toast.complete"), getTranslation(END_DL_DESCRIPTION, action));
            } catch (IOException e) {
                sendToastMessage(Text.translatable("mcbrowser.download.toast.failed"), Text.translatable("mcbrowser.download.toast.failed.description"));
                MCBrowser.LOGGER.error("Failed to download file", e);
            }
        });
    }


    private static String cleanseFileUrl(String url) {
        String[] array = url.split("/");
        return array[array.length - 1].replace("%2B", "+");
    }

    private static String getModrinthSlugFromUrl(String url) {
        String string = url.replace("https://modrinth.com/", "");
        string = string.substring(string.indexOf("/") + 1);
        return string.split("/")[0];
    }

    private static void sendToastMessage(Text title, Text description) {
        MinecraftClient.getInstance().getToastManager().add(new SystemToast(new SystemToast.Type(), title, description));
    }

    private static MutableText getTranslation(byte type, SpecialButtonActions action) {
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

    private static URL getTargetURL(SpecialButtonActions action) throws MalformedURLException {
        return switch (action) {
            case MODRINTH_MOD -> new URL("https://api.modrinth.com/v2/project/" + getModrinthSlugFromUrl(TabManager.getCurrentUrl()) + "/version?game_versions=[%22" + MinecraftVersion.CURRENT.getName() + "%22]&loaders=[%22fabric%22]");
            case MODRINTH_RP -> new URL("https://api.modrinth.com/v2/project/" + getModrinthSlugFromUrl(TabManager.getCurrentUrl()) + "/version?game_versions=[%22" + MinecraftVersion.CURRENT.getName() + "%22]");

            //Reserved for future usage.
            //noinspection UnnecessaryDefault
            default -> throw new IllegalStateException("Unexpected action value: " + action);
        };
    }

    private static String getTargetDirectory(SpecialButtonActions action) {
        return switch (action) {
            case MODRINTH_MOD -> "mods/";
            case MODRINTH_RP -> "resourcepacks/";
        };
    }
}
