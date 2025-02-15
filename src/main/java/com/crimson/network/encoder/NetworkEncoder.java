package com.crimson.network.encoder;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.util.CharsetUtil;
import net.sf.json.JSONObject;

import java.util.List;

public class NetworkEncoder {

    private static void write(Channel channel, String content) {
        channel.writeAndFlush(Unpooled.copiedBuffer(content + '\0', CharsetUtil.UTF_8), channel.voidPromise());
    }

    public static void dispatch(JSONObject jsonObject, Channel channel) {
        write(channel, jsonObject.toString());
    }

    public static void dispatch(JSONObject jsonObject, List<Channel> channels) {
        channels.forEach(channel -> write(channel, jsonObject.toString()));
    }

    public static void dispatchExcept(String params, Channel exceptChannel, List<Channel> channels) {
        channels.stream()
            .filter(channel -> !channel.equals(exceptChannel))
            .forEach(channel -> write(channel, params));
    }

}
