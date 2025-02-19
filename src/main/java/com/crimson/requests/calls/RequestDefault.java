package com.crimson.requests.calls;

import com.crimson.avatar.player.PlayerAvatar;
import com.crimson.interfaces.IRequest;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class RequestDefault implements IRequest {

    private static final Logger log = LoggerFactory.getLogger(RequestDefault.class);

    @Override
    public void onRequest(PlayerAvatar playerAvatar, JSONObject jsonObject) throws IOException {
        log.info("Default request called");
    }

}
