package com.crimson.bakuretsu.models.avatar.npc;

import com.crimson.bakuretsu.annotations.Cached;
import com.crimson.bakuretsu.annotations.Column;
import com.crimson.bakuretsu.annotations.Id;
import com.crimson.bakuretsu.annotations.Table;
import com.crimson.bakuretsu.core.Model;
import com.crimson.bakuretsu.models.avatar.IAvatarData;
import com.crimson.bakuretsu.models.user.User;

@Cached
@Table("npcs")
public class NPC extends Model<User> implements IAvatarData {

	@Id
	@Column(
		name = "id"
	)
	private int id;

	@Column(
		name = "name"
	)
	private String name;

	public int getId() {
		return this.id;
	}

	@Override
	public NPC setId(int id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	@Override
	public NPC setName(String name) {
		this.name = name;
		return this;
	}

}
