package com.crimson.interfaces;

import com.crimson.avatar.player.PlayerAvatar;
import net.sf.json.JSONObject;

import java.io.IOException;

public interface IRequest {

    void onRequest(PlayerAvatar playerAvatar, JSONObject jsonObject) throws IOException;

}
