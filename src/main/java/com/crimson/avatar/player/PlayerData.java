package com.crimson.avatar.player;

import com.crimson.avatar.AvatarData;

public class PlayerData extends AvatarData {

    private final int id;
    private final String name;

    public PlayerData(int networkId, String name) {
        this.id = networkId;
        this.name = name;
    }

    @Override
    public int id() {
        return this.id;
    }

    @Override
    public String name() {
        return this.name;
    }

}
