package com.crimson.avatar.player;

import com.crimson.avatar.Avatar;
import com.crimson.avatar.AvatarNetwork;
import com.crimson.bakuretsu.models.avatar.user.User;
import com.crimson.interfaces.IDispatchable;
import io.netty.channel.Channel;
import net.sf.json.JSONObject;

public class Player extends Avatar<User> implements IDispatchable {

	private final AvatarNetwork network;
	public final User data;

	public Player(int networkId, String name, Channel channel, User data) {
		this.network = new AvatarNetwork(networkId, name, channel);
		this.data = data;
	}

	@Override
	public User data() {
		return this.data;
	}

	public AvatarNetwork network() {
		return this.network;
	}

    @Override
    public void dispatch(JSONObject params) {

    }

}
