package com.confusinguser.sbgods.entities;

public class PlayerAH {
    private AhItem[] items;
    private String error;
    private boolean isError = false;

    public PlayerAH(AhItem[] items) {
        this.items = items;
    }

    public PlayerAH(String error) {
        this.error = error;
        this.isError = true;
    }

    public boolean isError() {
        return isError;
    }

    public String getError() {
        return error;
    }

    public AhItem[] getItems() {
        return items;
    }
}
