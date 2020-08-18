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
        ServerSocket s;
        try {
            s = new ServerSocket(34583, 10, InetAddress.getLocalHost());
        } catch (IOException e) {
            // Port taken, so app is already running
            System.out.print("Application is most likely already running");
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

        /*Thread listenerThread = new Thread(() -> {
            while (true) {
                try {
                    Socket socket = s.accept();
                    Thread socketThread = new Thread(() -> {
                        BufferedReader bufferedReader;
                        try {
                            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                            return;
                        }
                        while (true) {
                            String data;
                            try {
                                data = bufferedReader.readLine();
                                if (data == null) {
                                    socket.close();
                                    break;
                                }
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                                return;
                            }
                            System.out.println(data);
                        }
                    });

                    socketThread.start();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        listenerThread.start();*/
    }
}
