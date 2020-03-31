package com.confusinguser.sbgods.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.regex.Pattern;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.entities.Response;

public class CacheUtil {

	private static final long MAX_CACHE_TIME = 10 * 1000 * 60;

	// private SBGods main;

	private ArrayList<Response> cache = new ArrayList<Response>();

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

	public boolean isCached(String url) {
		long currentTime = new Date().getTime();
		for (Response response : cache) {
			if (response.getURL().contentEquals(url)) {
				if ((response.getTimeStamp() - currentTime) > MAX_CACHE_TIME ) {
					cache.remove(cache.indexOf(response));
					return false;
				} else {
					return true;
				}
			}
		}
		return false;
	}

	public Response getCachedResponse(String url) {
		long currentTime = new Date().getTime();
		Iterator<Response> it = cache.iterator();
		while(it.hasNext()) {
			Response response = it.next();
			if ((response.getTimeStamp() - currentTime) > MAX_CACHE_TIME) {
				cache.remove(cache.indexOf(response));
			}
			if (response.getURL().contentEquals(url)) {
				if ((response.getTimeStamp() - currentTime) < MAX_CACHE_TIME) {
					return response;
				} else {
					cache.remove(cache.indexOf(response));
				}
			}
		}
		return new Response(null, null);
	}

	public void addToCache(String url, String json) {
		cache.add(new Response(url, json));
	}
}
