package com.confusinguser.sbgods;

import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.HypixelGuild;
import org.json.JSONObject;

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
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(35746, 10, InetAddress.getLocalHost());
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
                    Socket socket = serverSocket.accept();
                    Thread socketThread = new Thread(() -> {
                        DataInputStream dataInputStream = null;
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
                        JSONObject jsonData = new JSONObject(data);
                        String author = jsonData.getString("author");
                        String message = jsonData.getString("message");
                        DiscordServer discordServer = DiscordServer.getDiscordServerFromHypixelGuild(HypixelGuild.getGuildById(sbgods.getApiUtil().getGuildIDFromUUID(jsonData.getString("senderUUID"))), true);
                        if (discordServer == null) return;
                        sbgods.getUtil().handleGuildMessage(sbgods.getDiscord(), discordServer, author, message, ipAddr);
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
