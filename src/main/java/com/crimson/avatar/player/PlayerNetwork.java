package com.crimson.avatar.player;

import io.netty.channel.Channel;

public record PlayerNetwork(int networkId, String name, Channel channel) {

    public void dispatch() {
    }

    public void disconnect() {
    }

}
