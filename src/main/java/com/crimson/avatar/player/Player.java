package com.crimson.avatar.player;

import com.crimson.avatar.Avatar;
import com.crimson.bakuretsu.models.avatar.user.User;
import com.crimson.interfaces.IDispatchable;
import com.crimson.network.encoder.NetworkEncoder;
import io.netty.channel.Channel;
import net.sf.json.JSONObject;

public class Player extends Avatar<User> implements IDispatchable {

	private final int networkId;
	private final String name;
	private final Channel channel;
	private final User data;

	public Player(int networkId, String name, Channel channel, User data) {
		this.networkId = networkId;
		this.name = name;
		this.channel = channel;
		this.data = data;
	}

	public int networkId() {
		return networkId;
	}

	public String name() {
		return name;
	}

	public Channel channel() {
		return channel;
	}

	@Override
	public User data() {
		return this.data;
	}

	@Override
	public void dispatch(JSONObject params) {
		NetworkEncoder.dispatch(params, channel());
	}

}
