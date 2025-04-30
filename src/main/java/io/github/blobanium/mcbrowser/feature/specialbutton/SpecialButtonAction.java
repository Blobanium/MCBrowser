package io.github.blobanium.mcbrowser.feature.specialbutton;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.blobanium.mcbrowser.MCBrowser;
import io.github.blobanium.mcbrowser.util.SwitchFunctions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URI;
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
        MCBrowser.sendToastMessage(Text.translatable("mcbrowser.download.toast.started"), SwitchFunctions.SpecialButtonActionSwitches.getTranslation(START_DL_DESCRIPTION, action));

        CompletableFuture.runAsync(() -> {
            try {
                //Get the file from modrinth's API
                URL url = SwitchFunctions.SpecialButtonActionSwitches.getTargetURL(action);
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
                FileUtils.copyURLToFile(downloadURL, new File(FabricLoader.getInstance().getGameDir().toFile(), SwitchFunctions.SpecialButtonActionSwitches.getTargetDirectory(action) + cleanseFileUrl(downloadURL.getFile())));

                MCBrowser.sendToastMessage(Text.translatable("mcbrowser.download.toast.complete"), SwitchFunctions.SpecialButtonActionSwitches.getTranslation(END_DL_DESCRIPTION, action));
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
}
