package com.crimson.avatar;

import io.netty.channel.Channel;
import org.json.JSONObject;

public record AvatarNetwork(int networkId, String name, Channel channel) {

    public void dispatch(JSONObject jsonObject) {
    }

    public void disconnect() {
    }

}
