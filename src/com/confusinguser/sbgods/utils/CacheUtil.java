package com.confusinguser.sbgods.utils;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.entities.Response;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;

public class CacheUtil {

    //private static final long MAX_CACHE_TIME = 86400000; //1 day store, probs wont use the full day
    private static final long MAX_CACHE_TIME = 900000; //15 min store

    // private SBGods main;

    private final List<Response> cache = new ArrayList<>();

    public CacheUtil(SBGods main) {
        // this.main = main;
    }

    public String stripUnnecesaryInfo(String url_string) {
        Matcher matcher = RegexUtil.getMatcher("(?:&|)key=[0-9A-Za-z\\-]{36}(?:&|)", url_string);
        String output = matcher.replaceAll("&").replace("https://", "").replace("http://", "");
        if (output.endsWith("&")) {
            output = output.substring(0, output.length() - 1);
        }
        return output;
    }

    public Response getCachedResponse(String url, long cacheTime) {
        long currentTime = System.currentTimeMillis();

        Iterator<Response> it = cache.iterator();
        while (it.hasNext()) {
            Response response = it.next();
            if (currentTime - (response.getTimeStamp()) > MAX_CACHE_TIME) {
                it.remove();
            }
            if (response.getURL().contentEquals(url)) {
                if ((currentTime - response.getTimeStamp()) < Math.min(MAX_CACHE_TIME, cacheTime)) {
                    return response;
                }
            }
        }
        return null;
    }

    public void addToCache(String url, String json) {
        cache.add(new Response(url, json));
    }
}
