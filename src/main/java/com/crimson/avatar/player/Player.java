package com.crimson.avatar.player;

import com.crimson.avatar.Avatar;
import io.netty.channel.Channel;

public class Player extends Avatar<PlayerData> {

    public static final Player NONE = new Player(-1, "", null);

    private final PlayerNetwork network;
    public final PlayerData data;

    public Player(int networkId, String name, Channel channel) {
        this.data = new PlayerData(networkId, name);
        this.network = new PlayerNetwork(channel);
    }

    @Override
    public PlayerData data() {
        return this.data;
    }

    public PlayerNetwork network() {
        return this.network;
    }

}
