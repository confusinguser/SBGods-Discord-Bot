package com.confusinguser.sbgods.entities;

import com.confusinguser.sbgods.SBGods;

import java.util.ArrayList;

public class PlayerAH {

    private AHItem[] items = new AHItem[10];

    public int length = 0;

    private String error;
    private Boolean isError = false;

    public PlayerAH(AHItem[] items) {
        this.items = items;
    }
    public PlayerAH(String error) {
        this.error = error;
        this.isError = true;
    }

    public boolean getIsError() {
        return isError;
    }
    public String getError() {
        return error;
    }
    public AHItem[] getItems() {
        return items;
    }
    public PlayerAH setLength(int length) {
        this.length = length;return this;
    }

}
