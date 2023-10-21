package io.github.blobanium.mcbrowser.feature;

public class BrowserUtil {
    public static String prediffyURL(String url){
        //See if it has the scheme (aka where it says "http" or https), were only doing this because there are URL schemes other than http and https.
        if(url.contains("://")){
            return url;

        }else if(url.contains(".") && !url.contains(" ")){
            //See if this is an actual link a user typed in manually, if it is then append HTTP to the beginning
            return "http://" + url; //This should default to HTTPS if it's A HTTPS Site
        }else{
            //Treat if this is a google search.
            return searchToURL(url);
        }
    }

    private static String searchToURL(String search){
        //This is for when we can give the user the option to choose what search engine they want to use in the future but for now use google.
        String query = search.replace(" ", "+");
        return "https://www.google.com/search?q=" + query;
    }
}

