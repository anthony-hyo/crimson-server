package com.crimson.network.channel;

import com.crimson.Main;
import com.crimson.network.decoder.NetworkDecoder;
import com.crimson.network.handler.NetworkHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;

public class NetworkInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) {
        socketChannel.pipeline()
            .addLast("traffic", Main.GLOBAL_TRAFFIC_SHAPING_HANDLER)
            .addLast("framer", new DelimiterBasedFrameDecoder(4096, Delimiters.nulDelimiter()))
            .addLast("gameDecoder", new NetworkDecoder())
            .addLast("handler", new NetworkHandler());
    }

}
