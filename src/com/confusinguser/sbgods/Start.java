package com.confusinguser.sbgods;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.awt.*;
import java.io.Console;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

class Start {

    public static void main(String[] args) throws UnsupportedEncodingException {
        ServerSocket s;
        try {
            s = new ServerSocket(34583, 10, InetAddress.getLocalHost());
        } catch (IOException e) {
            // Port taken, so app is already running
            System.out.println("Application is most likely already running");
            System.exit(1);
            return;
        }

        Console console = System.console();
        boolean logTerminalError = false;
        if (console == null && !GraphicsEnvironment.isHeadless()) {
            String filename = URLDecoder.decode(Start.class.getProtectionDomain().getCodeSource().getLocation().toString().substring(6), StandardCharsets.UTF_8.toString());
            if (filename.endsWith(".jar")) {
                try {
                    Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", "cmd", "/k", "java -jar \"" + filename + "\""});
                    System.exit(0);
                } catch (IOException e) {
                    logTerminalError = true;
                }
            }
        }

        SBGods sbgods = new SBGods();
        if (logTerminalError) sbgods.logger.info("Could not open terminal");

        Thread listenerThread = new Thread(() -> {
            while (true) {
                try {
                    Socket socket = s.accept();
                    Thread socketThread = new Thread(() -> {
                        DataInputStream dataInputStream = null;
                        String author;
                        String message;
                        String data = "";
                        InetAddress ipAddr = socket.getInetAddress();
                        try {
                            dataInputStream = new DataInputStream(socket.getInputStream());
                            data = dataInputStream.readUTF();
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                            try {
                                socket.close();
                                if (dataInputStream != null)
                                    dataInputStream.close();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                                return;
                            }
                        }
                        System.out.println(data);
                        JsonObject jsonData = JsonParser.parseString(data).getAsJsonObject();
                        author = jsonData.get("author").getAsString();
                        message = jsonData.get("message").getAsString();
                        sbgods.getUtil().handleGuildMessage(sbgods.getDiscord(), author, message, ipAddr);
                    });
                    socketThread.start();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                    return;
                }
            }
        });
        listenerThread.start();
    }
}
