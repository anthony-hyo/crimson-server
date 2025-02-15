package com.crimson.network;

import com.crimson.network.channel.NetworkInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public record Network(String ip, int port, ServerBootstrap serverBootstrap) {

    private static final Logger log = LoggerFactory.getLogger(Network.class);

    public void createSocket() {
        EventLoopGroup bossGroup = Epoll.isAvailable() ? new EpollEventLoopGroup(1) : new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = Epoll.isAvailable() ? new EpollEventLoopGroup(1) : new NioEventLoopGroup(1);

        serverBootstrap().group(bossGroup, workerGroup)
            .channel(Epoll.isAvailable() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
            .childHandler(new NetworkInitializer())
            .childOption(ChannelOption.TCP_NODELAY, true)
            .childOption(ChannelOption.SO_KEEPALIVE, true)
            .childOption(ChannelOption.SO_RCVBUF, 5120)
            .childOption(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(5120))
            .childOption(ChannelOption.ALLOCATOR, new UnpooledByteBufAllocator(false))
            .childOption(ChannelOption.SO_REUSEADDR, true);
    }

    public void bind() {
        serverBootstrap().bind(new InetSocketAddress(this.ip(), this.port())).addListener(objectFuture -> {
            if (!objectFuture.isSuccess()) {
                log.error("Failed to start server on address: {}:{}", this.ip(), this.port());
            } else {
                log.info("Ready for connections on address: {}:{}", this.ip(), this.port());
            }
        });
    }

}
