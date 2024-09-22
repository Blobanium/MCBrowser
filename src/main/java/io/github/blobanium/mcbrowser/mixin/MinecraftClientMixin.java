package io.github.blobanium.mcbrowser.mixin;

import io.github.blobanium.mcbrowser.MCBrowser;
import io.github.blobanium.mcbrowser.util.TabManager;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.version.VersionComparisonOperator;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(at = @At(value = "HEAD"), method = "close")
    private void onClose(CallbackInfo ci) {
        MCBrowser.isShuttingDown = true;
        if (MCBrowser.getConfig().saveTabs) {
            TabManager.saveTabsToJson();
        }
        TabManager.reset();
    }

    /**
     * @author JetbrainsAI
     * Injected method to be called after the Minecraft client's 'close' method.
     * This method ensures that any lingering JCEF (Java Chromium Embedded Framework)
     * helper processes are terminated to prevent CPU resource usage after closing
     * the Minecraft client.
     * @author Blobanium
     * The lingering JCEF Processes are due to a bug with the MCEF library and this method
     * is only implemented as a temporary workaround and will be removed once MCEF corrects
     * the issue.
     */
    @Inject(at = @At("TAIL"), method = "close")
    private void onAfterClose(CallbackInfo ci) {
        try {
            if (VersionComparisonOperator.LESS_EQUAL.test(SemanticVersion.parse(FabricLoader.getInstance().getModContainer("mcef").get().getMetadata().getVersion().toString()), SemanticVersion.parse("2.1.5")) && System.getProperty("os.name").toLowerCase().contains("win")) {
                String processName = "jcef_helper.exe";
                ProcessBuilder processBuilder = new ProcessBuilder("tasklist");
                Process process = processBuilder.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                boolean isRunning = false;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(processName)) {
                        isRunning = true;
                        break;
                    }
                }
                reader.close();

                if (isRunning && MCBrowser.getConfig().killJcefHelperOnClose) {
                    MCBrowser.LOGGER.warn("JCEF Processes are still running when they should have been shut down, attempting to close them to ensure processes are terminated and dont use up CPU resources after closing minecraft.");
                    ProcessBuilder killProcess = new ProcessBuilder("taskkill", "/F", "/IM", processName);
                    killProcess.start();
                }
            }
        } catch (VersionParsingException | IOException e) {
            MCBrowser.LOGGER.fatal("JCEF Process Check Failed. There still may be lingering processes running in the background and eating up system resources. Please report this error to the developer.", e);
        }
    }
}
