package com.confusinguser.sbgods.entities;

public class PlayerAH {
    private AHItem[] items;
    private String error;
    private boolean isError = false;

    public PlayerAH(AHItem[] items) {
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

    public AHItem[] getItems() {
        return items;
    }
}
