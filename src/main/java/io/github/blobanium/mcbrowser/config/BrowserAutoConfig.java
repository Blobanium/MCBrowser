package io.github.blobanium.mcbrowser.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "mcbrowser")
public class BrowserAutoConfig implements ConfigData {
        String testString = "Foo";
}
