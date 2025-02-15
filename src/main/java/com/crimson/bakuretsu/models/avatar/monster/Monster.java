package com.crimson.bakuretsu.models.avatar.monster;

import com.crimson.bakuretsu.annotations.Column;
import com.crimson.bakuretsu.annotations.Id;
import com.crimson.bakuretsu.core.Model;
import com.crimson.bakuretsu.models.avatar.IAvatarData;
import com.crimson.bakuretsu.models.avatar.user.User;

public class Monster extends Model<User> implements IAvatarData {

	@Id
	@Column(name = "id")
	private int id;

	@Column(name = "Name")
	private String Name;

	@Override
	public int getId() {
		return id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String getName() {
		return Name;
	}

	@Override
	public void setName(String name) {
		Name = name;
	}

}
