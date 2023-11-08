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
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class SpecialButtonAction {
    public static void downloadModrinthMod(){
        sendToastMessage(MutableText.of(new TranslatableTextContent("mcbrowser.moddownload.toast.downloadstarted", "Download Started", TranslatableTextContent.EMPTY_ARGUMENTS)), MutableText.of(new TranslatableTextContent("mcbrowser.moddownload.toast.downloadstarted.description", "Please wait while your mod downloads.", TranslatableTextContent.EMPTY_ARGUMENTS)));

        CompletableFuture.runAsync(() -> {
            try {
                //Get the file from modrinth's API
                URL url = new URL("https://api.modrinth.com/v2/project/" + getModrinthSlugFromUrl(BrowserScreenHelper.currentUrl) + "/version?game_versions=[%22" + MinecraftVersion.CURRENT.getName() + "%22]&loaders=[%22fabric%22]");
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
                FileUtils.copyURLToFile(downloadURL, new File(FabricLoader.getInstance().getGameDir().toFile(), "mods/" + cleanseFileUrl(downloadURL.getFile())));

                sendToastMessage(MutableText.of(new TranslatableTextContent("mcbrowser.moddownload.toast.downloadcomplete", "Download Completed!", TranslatableTextContent.EMPTY_ARGUMENTS)), MutableText.of(new TranslatableTextContent("mcbrowser.moddownload.toast.downloadcomplete.description", "Restart your client for changes to take effect.", TranslatableTextContent.EMPTY_ARGUMENTS)));
            } catch (IOException e) {
                sendToastMessage(MutableText.of(new TranslatableTextContent("mcbrowser.moddownload.toast.downloadfailed", "Download Failed", TranslatableTextContent.EMPTY_ARGUMENTS)), MutableText.of(new TranslatableTextContent("mcbrowser.moddownload.toast.downloadfailed.description", "Check your logs for more info", TranslatableTextContent.EMPTY_ARGUMENTS)));
                e.printStackTrace();
            }
        });
    }

    public static void downloadModrinthRP(){
        sendToastMessage(MutableText.of(new TranslatableTextContent("mcbrowser.rpdownload.toast.downloadstarted", "Download Started", TranslatableTextContent.EMPTY_ARGUMENTS)), MutableText.of(new TranslatableTextContent("mcbrowser.rpdownload.toast.downloadstarted.description", "Please wait while your resource pack downloads.", TranslatableTextContent.EMPTY_ARGUMENTS)));

        CompletableFuture.runAsync(() -> {
            try {
                //Get the file from modrinth's API
                URL url = new URL("https://api.modrinth.com/v2/project/" + getModrinthSlugFromUrl(BrowserScreenHelper.currentUrl) + "/version?game_versions=[%22" + MinecraftVersion.CURRENT.getName() + "%22]");
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
                FileUtils.copyURLToFile(downloadURL, new File(FabricLoader.getInstance().getGameDir().toFile(), "resourcepacks/" + cleanseFileUrl(downloadURL.getFile())));

                sendToastMessage(MutableText.of(new TranslatableTextContent("mcbrowser.rpdownload.toast.downloadcomplete", "Download Completed!", TranslatableTextContent.EMPTY_ARGUMENTS)), MutableText.of(new TranslatableTextContent("mcbrowser.rpdownload.toast.downloadcomplete.description", "Resource Pack can be turned on in Resource Packs Settings", TranslatableTextContent.EMPTY_ARGUMENTS)));
            } catch (IOException e) {
                sendToastMessage(MutableText.of(new TranslatableTextContent("mcbrowser.rpdownload.toast.downloadfailed", "Download Failed", TranslatableTextContent.EMPTY_ARGUMENTS)), MutableText.of(new TranslatableTextContent("mcbrowser.rpdownload.toast.downloadfailed.description", "Check your logs for more info", TranslatableTextContent.EMPTY_ARGUMENTS)));
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
}
