package com.confusinguser.sbgods.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

import com.confusinguser.sbgods.SBGods;

public class JsonApiUtil {

	SBGods main;

	private final String USER_AGENT = "Mozilla/5.0";
	private final String BASE_URL = "https://api.myjson.com/";
	private final String BIN_ID = "lnta6";

	public JsonApiUtil(SBGods main) {
		this.main = main;
	}

	private int fails = 0;

	public String getResponse(String url_string) {
		StringBuffer response = null;
		try {
			URL url = new URL(url_string);

			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", USER_AGENT);

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			response = new StringBuffer();

			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

		} catch (IOException e) {
			fails++;
			if (fails % 10 == 0) {
				main.logInfo("Failed to connect to the Json API " + fails + " times, this may be a problem...");
			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e1) {
				e.printStackTrace();
			}
			return getResponse(url_string);
		}
		return response.toString();
	}

	public String updateSettings() {
		JSONObject data = new JSONObject().append("commandPrefix", main.getDiscord().commandPrefix);
		StringBuffer response = null;
		try {
			URL url = new URL(BASE_URL + "bins/" + BIN_ID);

			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("PUT");
			con.setRequestProperty("User-Agent", USER_AGENT);

			OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
			out.write(data.toString());
			out.close();

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			response = new StringBuffer();

			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
		} catch (IOException e) {
			main.logInfo("Could not upload new settings");
		}
		return response.toString();
	}

	public String getSettings() {
		String response = getResponse(BASE_URL + "bins/" + BIN_ID);
		return response;
	}
}
