package com.confusinguser.sbgods.utils;

import com.confusinguser.sbgods.SBGods;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class JsonApiUtil {

    private final SBGods main;

    private final String USER_AGENT = "Mozilla/5.0";
    private final String BASE_URL = "https://api.myjson.com/";
    private final String BIN_ID = "lnta6";
    private int fails = 0;

    public JsonApiUtil(SBGods main) {
        this.main = main;
    }

    private String getResponse(String url_string) {
        StringBuffer response;
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
                main.logger.info("Failed to connect to the Json API " + fails + " times, this may be a problem...");
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

    public void updateSettings() {
        JSONObject data = new JSONObject().append("commandPrefix", main.getDiscord().commandPrefix);
        StringBuffer response;
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
            main.logger.info("Could not upload new settings");
        }
    }

    public String getBotData() {
        return getResponse(BASE_URL + "bins/" + BIN_ID);
    }
}
