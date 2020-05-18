package com.confusinguser.sbgods;

import java.awt.*;
import java.io.Console;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URLDecoder;
import java.net.UnknownHostException;

class Start {

    public static void main(String[] args) throws UnsupportedEncodingException {
        try {
            ServerSocket s = new ServerSocket(65000, 10, InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
            // Shouldn't happen for localhost
        } catch (IOException e) {
            // Port taken, so app is already running
            System.out.print("Application is probably already running, it would be pretty weird with 2 bots at the same time wouldn't it?");
            System.exit(0);
        }

        Console console = System.console();
        if (console == null && !GraphicsEnvironment.isHeadless()) {
            String filename = URLDecoder.decode(Start.class.getProtectionDomain().getCodeSource().getLocation().toString().substring(6), "UTF-8");
            if (filename.endsWith(".jar")) {
                try {
                    Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", "cmd", "/k", "java -jar \"" + filename + "\""});
                    System.exit(0);
                } catch (IOException e) {
                    try {
                        SBGods.getInstance().logger.warning("Could not open terminal");
                    } catch (NullPointerException er) {SBGods.getInstance().logWorks = false;}
                }
            }
        }

        SBGods sbgods = new SBGods();
        LeaderboardUpdater updater = new LeaderboardUpdater();
        Thread updaterThread = new Thread(updater);
        updaterThread.start();
    }
}
