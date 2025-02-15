package com.crimson.bakuretsu.models.avatar.user;

import com.crimson.bakuretsu.annotations.Column;
import com.crimson.bakuretsu.annotations.Id;
import com.crimson.bakuretsu.annotations.Table;
import com.crimson.bakuretsu.core.Model;
import com.crimson.bakuretsu.models.avatar.IAvatarData;

@Table("users")
public class User extends Model<User> implements IAvatarData {

	@Id
	@Column(
		name = "id"
	)
	private int id;

	@Column(
		name = "Name"
	)
	private String Name;

	@Column(
		name = "Password"
	)
	private String Password;

	@Override
	public int getId() {
		return id;
	}

	@Override
	public User setId(int id) {
		this.id = id;
		return this;
	}

	@Override
	public String getName() {
		return Name;
	}

	@Override
	public User setName(String name) {
		Name = name;
		return this;
	}

	public String getPassword() {
		return Password;
	}

	public User setPassword(String password) {
		Password = password;
		return this;
	}

}
