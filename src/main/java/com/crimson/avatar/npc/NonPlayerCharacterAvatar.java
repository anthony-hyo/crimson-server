package com.crimson.avatar.npc;

import com.crimson.avatar.Avatar;
import com.crimson.avatar.player.PlayerData;

public class NonPlayerCharacterAvatar extends Avatar<PlayerData> {

	@Override
	public PlayerData data() {
		return null;
	}

}
