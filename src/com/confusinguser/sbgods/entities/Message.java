package com.confusinguser.sbgods.entities;

import com.confusinguser.sbgods.SBGods;

public enum Message {
    NO_PERMS("You don't have permission to perform this comamnd");

    private final SBGods main = SBGods.getInstance();

    private final String message;

    Message(String message) {
        this.message = message;
    }

    public String getMessage(){return message;}
}
