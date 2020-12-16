package com.confusinguser.sbgods;

import java.awt.*;
import java.io.Console;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

class Start {

    public static void main(String[] args) throws UnsupportedEncodingException {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(35746, 10, InetAddress.getByName("0.0.0.0"));
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
                } catch (IOException ignored) {
                }
            }
        }

        try {
            new SBGods(serverSocket);
        } catch (Throwable t) {
            if (SBGods.getInstance() != null && SBGods.getInstance().getDiscord() != null) {
                SBGods.getInstance().getDiscord().reportFail(t, "General Bot");
            }
        }
    }
}
