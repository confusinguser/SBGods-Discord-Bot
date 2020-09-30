package com.confusinguser.sbgods;

import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.HypixelGuild;
import com.confusinguser.sbgods.utils.EncryptionUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
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

        EncryptionUtil encryptionUtil = new EncryptionUtil();
        Thread listenerThread = new Thread(() -> {
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    Thread socketThread = new Thread(() -> {
                        DataInputStream dataInputStream;
                        String data;
                        InetAddress ipAddr = socket.getInetAddress();
                        JsonObject parsedJson;
                        try {
                            dataInputStream = new DataInputStream(socket.getInputStream());
                            data = DataInputStream.readUTF(dataInputStream);
                            parsedJson = JsonParser.parseString(data).getAsJsonObject();
                        } catch (IOException | JsonParseException ex) {
                            ex.printStackTrace();
                            return;
                        }

                        //<editor-fold desc="Encryption Attempt that wasn't successful :(">
/*                        // Encryption Attempt that didn't wasn't successful :(
                        PrivateKey privateKey = encryptionUtil.getKeyForIP(ipAddr);
                        try {
                            if (privateKey == null || parsedJson.get("needPublicKey") != null && parsedJson.get("needPublicKey").getAsBoolean()) {
                                KeyPair keyPair = encryptionUtil.generateKeyPair();

                                JsonObject sendBackJson = new JsonObject();
                                sendBackJson.addProperty("encryptionKey", new String(Base64.getEncoder().encode(keyPair.getPublic().getEncoded())));
                                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                                dataOutputStream.writeUTF(sendBackJson.toString());

                                encryptionUtil.assignKeyForIP(ipAddr, keyPair.getPrivate());
                                return;
                            }
                            String decryptedData = encryptionUtil.decryptText(data, privateKey);

                            dataInputStream.close();
                            socket.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            return;
                        } catch (BadPaddingException | IllegalBlockSizeException | InvalidKeyException e) {
                            e.printStackTrace();
                            KeyPair keyPair = encryptionUtil.generateKeyPair();

                            JsonObject sendBackJson = new JsonObject();
                            sendBackJson.addProperty("encryptionKey", new String(Base64.getEncoder().encode(keyPair.getPublic().getEncoded())));
                            DataOutputStream dataOutputStream;
                            encryptionUtil.assignKeyForIP(ipAddr, keyPair.getPrivate());
                            try {
                                dataOutputStream = new DataOutputStream(socket.getOutputStream());
                                dataOutputStream.writeUTF(sendBackJson.toString());
                            } catch (IOException exception) {
                                exception.printStackTrace();
                            }

                            encryptionUtil.assignKeyForIP(ipAddr, keyPair.getPrivate());
                            return;
                        }
 */
                        //</editor-fold>

                        String message = parsedJson.get("message").getAsString();
                        DiscordServer discordServer = DiscordServer.getDiscordServerFromHypixelGuild(HypixelGuild.getGuildById(sbgods.getApiUtil().getGuildIDFromUUID(parsedJson.get("senderUUID").getAsString())), true);
                        if (discordServer == null) return;
                        sbgods.getUtil().handleGuildMessage(sbgods.getDiscord(), discordServer, sbgods.getUtil().getAuthorFromGuildChatMessage(message), sbgods.getUtil().getMessageFromGuildChatMessage(message), ipAddr);
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
