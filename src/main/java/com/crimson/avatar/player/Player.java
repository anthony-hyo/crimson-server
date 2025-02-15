package com.crimson.avatar.player;

import com.crimson.avatar.Avatar;
import com.crimson.bakuretsu.models.avatar.user.User;
import io.netty.channel.Channel;

public class Player extends Avatar<User> {

    private final PlayerNetwork network;
    public final User data;

    public Player(int networkId, String name, Channel channel, User data) {
        this.network = new PlayerNetwork(networkId, name, channel);
	    this.data = data;
    }

    @Override
    public User data() {
        return this.data;
    }

    public PlayerNetwork network() {
        return this.network;
    }

}
