package com.crimson.network.handler;

import com.crimson.avatar.player.Player;
import com.crimson.controller.PlayerController;
import com.crimson.network.data.JsonData;
import com.crimson.requests.RequestFactory;
import com.crimson.requests.data.RequestData;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.sf.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkHandler extends SimpleChannelInboundHandler<JsonData> {

    private static final Logger log = LoggerFactory.getLogger(NetworkHandler.class);

    private static final String REQUEST = "request";
    private static final String EVENT = "event";

    private static final String REQUEST_NAME = "cmd";
    private static final String REQUEST_JSON = "args";

    private static final String EVENT_LOGIN = "login";
    private static final String EVENT_REGISTER = "register";

    private static void handleRequest(ChannelHandlerContext ctx, JSONObject json) {
        Player player = ctx.channel().attr(PlayerController.PLAYER_KEY).get();

        if (player == null) {
            handlePlayerNotFound(ctx);
            return;
        }

        RequestFactory.get(json.getString(REQUEST_NAME)).run(player, json.getJSONObject(REQUEST_JSON));
    }

    private static void handleEvent(ChannelHandlerContext ctx, JSONObject json) {
        String cmd = json.getString(REQUEST_NAME);

        switch (cmd) {
            case EVENT_LOGIN -> PlayerController.login(ctx.channel(), json.getJSONObject(REQUEST_JSON));
            case EVENT_REGISTER -> PlayerController.register(ctx.channel(), json.getJSONObject(REQUEST_JSON));
            default -> throw new IllegalStateException("Unexpected event: " + cmd);
        }
    }

    private static void handlePlayerNotFound(ChannelHandlerContext ctx) {
        String ip = getIpAddress(ctx);

        log.warn("Player does not exist for IP: {}", ip);

        ctx.disconnect();
    }

    private static void handleException(ChannelHandlerContext ctx, Exception ex) {
        log.error("Failed to handle json. Reason: {}", ex.getMessage());

        if (ctx.channel().isActive()) {
            ctx.close();
        }
    }

    private static String getIpAddress(ChannelHandlerContext ctx) {
        return ctx.channel().remoteAddress().toString().replace("/", "").split(":")[0];
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, JsonData jsonData) {
        try {
            JSONObject json = jsonData.json();

            switch (jsonData.type()) {
                case REQUEST -> handleRequest(ctx, json);
                case EVENT -> handleEvent(ctx, json);
                default -> throw new IllegalStateException("Unexpected value: " + jsonData.type());
            }
        } catch (Exception ex) {
            handleException(ctx, ex);
        }
    }


    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        String ip = getIpAddress(ctx);
        log.debug("Connection established from IP: {}", ip);
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) {
        String ip = getIpAddress(ctx);

        Player player = ctx.channel().attr(PlayerController.PLAYER_KEY).get();

        if (player == null) {
            log.debug("Disconnected IP with NULL player: {}", ip);
            return;
        }

        log.info("Player disconnected - Username: {}, Network ID: {}, IP: {}", player.data().name(), player.data().id(), ip);

        player.network().disconnect();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        String ip = getIpAddress(ctx);

        log.debug("Netty error occurred for IP: {}. Reason: {}", ip, cause.getMessage(), cause);

        ctx.close();
    }
}
