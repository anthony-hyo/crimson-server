package com.crimson.avatar.player;

import io.netty.channel.Channel;

public record PlayerNetwork(Channel channel) {

    public void dispatch() {
    }

    public void disconnect() {
    }

}
