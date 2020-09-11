package com.confusinguser.sbgods.entities;

import com.confusinguser.sbgods.SBGods;

public enum Message {
    // THIS IS NOT THE WAY TO DO THIS!!! https://www.baeldung.com/java-localization-messages-formatting
    NO_PERMS("You don't have permission to perform this comamnd");

    private final SBGods main = SBGods.getInstance();

    private final String message;

    Message(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
