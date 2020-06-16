package com.confusinguser.sbgods.utils;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.entities.Response;

import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

public class CacheUtil {

    //private static final long MAX_CACHE_TIME = 86400000; //1 day store, probs wont use the full day
    private static final long MAX_CACHE_TIME = 300; //5 min store

    // private SBGods main;

    private final ArrayList<Response> cache = new ArrayList<>();

    public CacheUtil(SBGods main) {
        // this.main = main;
    }

    public String stripUnnecesaryInfo(String url_string) {
        Pattern pattern = Pattern.compile("key=[0-9A-Za-z\\-]{36}");
        String output = pattern.matcher(url_string).replaceAll("").replace("https://", "").replace("http://", "").replace("?&", "?").replace("&&", "&");
        if (output.endsWith("&")) {
            output = output.substring(0, output.length() - 1);
        }
        return output;
    }

    public boolean isCached(String url, long cacheTime) {
        long currentTime = new Date().getTime();
        for (Response response : cache) {
            if (response.getURL().contentEquals(url)) {
                return (currentTime - (response.getTimeStamp()) <= Math.min(cacheTime, MAX_CACHE_TIME));
            }
        }
        return false;
    }

    public Response getCachedResponse(String url, long cacheTime) {
        long currentTime = new Date().getTime();

        for (int i = 0; i < cache.size(); i++) {
            Response response = cache.get(i);
            if (currentTime - (response.getTimeStamp()) > MAX_CACHE_TIME) {
                cache.remove(response);
                i--;
            }
            if (response.getURL().contentEquals(url)) {
                if ((currentTime - response.getTimeStamp()) < Math.min(MAX_CACHE_TIME, cacheTime)) {
                    return response;
                }
            }
        }
        return new Response(null, null);
    }

    public void addToCache(String url, String json) {
        cache.add(new Response(url, json));
    }
}
