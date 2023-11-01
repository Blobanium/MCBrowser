package io.github.blobanium.mcbrowser.util;

import com.cinemamod.mcef.MCEFBrowser;
import com.cinemamod.mcef.MCEFClient;

public class BrowserImpl extends MCEFBrowser {
    public BrowserImpl(MCEFClient client, String url, boolean transparent) {
        super(client, url, transparent);
    }

    //This is here for future use in case i need to tweak some things within MCEFBrowser itself
}
