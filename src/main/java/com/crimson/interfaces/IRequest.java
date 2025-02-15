package com.crimson.interfaces;

import com.crimson.avatar.player.Player;
import net.sf.json.JSONObject;

import java.io.IOException;

public interface IRequest {

    void onRequest(Player player, JSONObject jsonObject) throws IOException;

}
