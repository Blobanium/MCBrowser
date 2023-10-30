package io.github.blobanium.mcbrowser.feature.specialbutton;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.MinecraftVersion;
import net.minecraft.client.MinecraftClient;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class SpecialButtonAction {
    public static String currentUrl = null;

    public static void downloadModrinthMod(){
        try {
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
        }catch (IOException e){
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
