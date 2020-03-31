package com.confusinguser.sbgods.entities;

import java.util.Date;

public class Response {
	
	private String url;
	private String json;
	private long timeStamp;

	public Response(String url, String json) {
		this.url = url;
		this.json = json;
		this.timeStamp = new Date().getTime();
	}

	public String getURL() {
		return url;
	}

	public String getJson() {
		return json;
	}

	public long getTimeStamp() {
		return timeStamp;
	}
}
