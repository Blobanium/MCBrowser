package io.github.blobanium.mcbrowser.feature.specialbutton;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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

public class SpecialButtonAction {
    public static String currentUrl = null;

    public static void downloadModrinthMod(){

        try {
            MinecraftClient.getInstance().getToastManager().add(new SystemToast(SystemToast.Type.TUTORIAL_HINT, MutableText.of(new TranslatableTextContent("mcbrowser.moddownload.toast.downloadstarted", "Download Started", TranslatableTextContent.EMPTY_ARGUMENTS)), MutableText.of(new TranslatableTextContent("mcbrowser.moddownload.toast.downloadstarted.description", "Please wait while your mod downloads.", TranslatableTextContent.EMPTY_ARGUMENTS))));

            //Get the file from modrinth's API
            URL url = new URL("https://api.modrinth.com/v2/project/" + getModrinthSlugFromUrl(currentUrl) + "/version?game_versions=[%22" + MinecraftVersion.CURRENT.getName() + "%22]&loaders=[%22fabric%22]");
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
            FileUtils.copyURLToFile(downloadURL, new File(FabricLoader.getInstance().getGameDir().toFile(), "mods/" + trimFileUrl(downloadURL.getFile())));

            MinecraftClient.getInstance().getToastManager().add(new SystemToast(SystemToast.Type.TUTORIAL_HINT, MutableText.of(new TranslatableTextContent("mcbrowser.moddownload.toast.downloadcomplete", "Download Completed!", TranslatableTextContent.EMPTY_ARGUMENTS)), MutableText.of(new TranslatableTextContent("mcbrowser.moddownload.toast.downloadcomplete.description", "Restart your client for changes to take effect.", TranslatableTextContent.EMPTY_ARGUMENTS))));
        }catch (IOException e){
            MinecraftClient.getInstance().getToastManager().add(new SystemToast(SystemToast.Type.TUTORIAL_HINT, MutableText.of(new TranslatableTextContent("mcbrowser.moddownload.toast.downloadfailed", "Download Failed", TranslatableTextContent.EMPTY_ARGUMENTS)), MutableText.of(new TranslatableTextContent( "mcbrowser.moddownload.toast.downloadfailed.description", "Check your logs for more info", TranslatableTextContent.EMPTY_ARGUMENTS))));
            e.printStackTrace();
        }
    }

    private static String trimFileUrl(String url){
        String[] array = url.split("/");
        return array[array.length-1];
    }

    private static String getModrinthSlugFromUrl(String url){
        String[] array =url.replace("https://modrinth.com/mod/", "").split("/");
        return array[0];
    }
}
