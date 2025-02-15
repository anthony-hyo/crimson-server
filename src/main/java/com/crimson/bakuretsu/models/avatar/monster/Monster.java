package com.crimson.bakuretsu.models.avatar.monster;

import com.crimson.bakuretsu.annotations.Cached;
import com.crimson.bakuretsu.annotations.Column;
import com.crimson.bakuretsu.annotations.Id;
import com.crimson.bakuretsu.annotations.Table;
import com.crimson.bakuretsu.core.Model;
import com.crimson.bakuretsu.models.avatar.IAvatarData;
import com.crimson.bakuretsu.models.avatar.user.User;

@Cached
@Table("monsters")
public class Monster extends Model<User> implements IAvatarData {

	@Id
	@Column(
		name = "id"
	)
	private int id;

	@Column(
		name = "Name"
	)
	private String Name;

	@Override
	public int getId() {
		return id;
	}

	@Override
	public Monster setId(int id) {
		this.id = id;
		return this;
	}

	@Override
	public String getName() {
		return Name;
	}

	@Override
	public Monster setName(String name) {
		Name = name;
		return this;
	}

}
