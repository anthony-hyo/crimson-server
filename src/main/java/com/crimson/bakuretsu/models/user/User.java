package com.crimson.bakuretsu.models.user;

import com.crimson.bakuretsu.annotations.Cached;
import com.crimson.bakuretsu.annotations.Column;
import com.crimson.bakuretsu.annotations.Id;
import com.crimson.bakuretsu.annotations.Table;
import com.crimson.bakuretsu.core.Model;

@Table("users")
@Cached
public class User extends Model<User> {

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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public String getPassword() {
		return Password;
	}

	public void setPassword(String password) {
		Password = password;
	}

}
