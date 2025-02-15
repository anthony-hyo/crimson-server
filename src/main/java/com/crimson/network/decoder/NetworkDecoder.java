package com.crimson.network.decoder;

import com.crimson.network.data.JsonData;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import kotlin.text.Charsets;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class NetworkDecoder extends MessageToMessageDecoder<ByteBuf> {

    private static final Logger log = LoggerFactory.getLogger(NetworkDecoder.class);

    private static final char JSON_START = '{';
    private static final String TYPE_KEY = "type";
    private static final String BODY_KEY = "body";

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) {
        String packet = byteBuf.toString(Charsets.UTF_8);

        byteBuf.markReaderIndex();

        if (byteBuf.readableBytes() < 2 || packet.charAt(0) != JSON_START) {
            handleInvalidPacket(ctx, packet);
            return;
        }

        log.debug("[RECEIVED] '{}'", packet);

        try {
            JSONObject jsonObject = JSONObject.fromObject(packet);
            out.add(new JsonData(jsonObject.getString(TYPE_KEY), jsonObject.getJSONObject(BODY_KEY)));
        } catch (Exception ex) {
            handleInvalidPacket(ctx, packet);
            log.error("Network decode error occurred", ex);
        }
    }

    private void handleInvalidPacket(ChannelHandlerContext ctx, String packet) {
        log.debug("Player {} sent an unknown/invalid packet: {}", ctx.channel().remoteAddress(), packet);

        if (ctx.channel().isActive()) {
            ctx.close();
        }
    }
}
