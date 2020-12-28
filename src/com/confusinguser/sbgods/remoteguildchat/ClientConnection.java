package com.confusinguser.sbgods.remoteguildchat;

import java.net.Socket;

public class ClientConnection {
    private final Socket socket;
    private final Object lock = new Object(); // For thread safety
    private String guildId;
    private String uuid;

    public ClientConnection(Socket socket) {
        this.socket = socket;
    }

    public Socket getSocket() {
        synchronized (lock) {
            return socket;
        }
    }

    public String getGuildId() {
        synchronized (lock) {
            return guildId;
        }
    }

    public void setGuildId(String guildId) {
        synchronized (lock) {
            this.guildId = guildId;
        }
    }

    public String getUuid() {
        synchronized (lock) {
            return uuid;
        }
    }

    public void setUuid(String uuid) {
        synchronized (lock) {
            this.uuid = uuid;
        }
    }
}
