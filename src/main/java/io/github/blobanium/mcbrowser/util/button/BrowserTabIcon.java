package io.github.blobanium.mcbrowser.util.button;

import com.cinemamod.mcef.MCEFClient;
import io.github.blobanium.mcbrowser.util.BrowserImpl;

public class BrowserTabIcon extends BrowserImpl {
    static final String apiUrl = "https://www.google.com/s2/favicons?sz=64&domain_url=";
    //TODO maybe replace the apiUrl thing with https://besticon-demo.herokuapp.com/allicons.json?url=URL,
    // cause it can help changing size of rendered field to match size of icon
    int size = 64;

    public BrowserTabIcon(MCEFClient client, String url, boolean transparent) {
        super(client, apiUrl + url, transparent);
        setSize(size);
    }

    public void setSize(int size) {
        this.size = size;
        this.resize(this.size, this.size);
    }
}
