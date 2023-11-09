package io.github.blobanium.mcbrowser.feature.specialbutton;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.blobanium.mcbrowser.util.BrowserScreenHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.MinecraftVersion;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
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
    public static void downloadModrinthMod(){
        downloadModrinth(SpecialButtonActions.MODRINTH_MOD);
    }

    public static void downloadModrinthRP(){
        downloadModrinth(SpecialButtonActions.MODRINTH_RP);
    }

    private static void downloadModrinth(SpecialButtonActions action){
        sendToastMessage(MutableText.of(new TranslatableTextContent("mcbrowser.download.toast.started.rp", "Download Started", TranslatableTextContent.EMPTY_ARGUMENTS)), getTranslation(START_DL_DESCRIPTION, action));

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

                sendToastMessage(MutableText.of(new TranslatableTextContent("mcbrowser.download.toast.complete", "Download Completed!", TranslatableTextContent.EMPTY_ARGUMENTS)), getTranslation(END_DL_DESCRIPTION, action));
            } catch (IOException e) {
                sendToastMessage(MutableText.of(new TranslatableTextContent("mcbrowser.download.toast.failed", "Download Failed", TranslatableTextContent.EMPTY_ARGUMENTS)), MutableText.of(new TranslatableTextContent("mcbrowser.download.toast.failed.description", "Check your logs for more info", TranslatableTextContent.EMPTY_ARGUMENTS)));
                e.printStackTrace();
            }
        });
    }


    private static String cleanseFileUrl(String url){
        String[] array = url.split("/");
        return array[array.length-1].replace("%2B", "+");
    }

    private static String getModrinthSlugFromUrl(String url){
        String string = url.replace("https://modrinth.com/", "");
        string = string.substring(string.indexOf("/") + 1);
        return string.split("/")[0];
    }

    private static void sendToastMessage(Text title, Text description){
        MinecraftClient.getInstance().getToastManager().add(new SystemToast(SystemToast.Type.TUTORIAL_HINT, title, description));
    }

    private static MutableText getTranslation(byte type, SpecialButtonActions action){
        return switch (action){
            case MODRINTH_MOD -> switch (type){
                case START_DL_DESCRIPTION -> MutableText.of(new TranslatableTextContent("mcbrowser.download.toast.started.description.mod", "Please wait while your mod downloads.", TranslatableTextContent.EMPTY_ARGUMENTS));
                case END_DL_DESCRIPTION -> MutableText.of(new TranslatableTextContent("mcbrowser.download.toast.complete.description.mod", "Restart your client for changes to take effect.", TranslatableTextContent.EMPTY_ARGUMENTS));
                default -> throw new IllegalStateException("Unexpected type value: " + type);
            };
            case MODRINTH_RP -> switch (type){
                case START_DL_DESCRIPTION -> MutableText.of(new TranslatableTextContent("mcbrowser.download.toast.started.description.rp", "Please wait while your resource pack downloads.", TranslatableTextContent.EMPTY_ARGUMENTS));
                case END_DL_DESCRIPTION ->  MutableText.of(new TranslatableTextContent("mcbrowser.download.toast.complete.description.rp", "Resource Pack can be turned on in Resource Packs Settings", TranslatableTextContent.EMPTY_ARGUMENTS));
                default -> throw new IllegalStateException("Unexpected type value: " + type);
            };

            //Reserved for future usage.
            default -> throw new IllegalStateException("Unexpected action value: " + action);
        };
    }

    private static URL getTargetURL(SpecialButtonActions action) throws MalformedURLException {
        return switch (action){
            case MODRINTH_MOD -> new URL("https://api.modrinth.com/v2/project/" + getModrinthSlugFromUrl(BrowserScreenHelper.currentUrl) + "/version?game_versions=[%22" + MinecraftVersion.CURRENT.getName() + "%22]&loaders=[%22fabric%22]");
            case MODRINTH_RP -> new URL("https://api.modrinth.com/v2/project/" + getModrinthSlugFromUrl(BrowserScreenHelper.currentUrl) + "/version?game_versions=[%22" + MinecraftVersion.CURRENT.getName() + "%22]");

            //Reserved for future usage.
            default -> throw new IllegalStateException("Unexpected action value: " + action);
        };
    }

    private static String getTargetDirectory(SpecialButtonActions action){
        return switch (action){
            case MODRINTH_MOD -> "mods/";
            case MODRINTH_RP -> "resourcepacks/";
        };
    }
}
