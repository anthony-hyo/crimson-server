package com.crimson.bakuretsu.models.user;

import com.crimson.bakuretsu.annotations.Column;
import com.crimson.bakuretsu.annotations.Id;
import com.crimson.bakuretsu.annotations.Table;
import com.crimson.bakuretsu.core.Model;

@Table("users")
public class User extends Model<User> {

	@Id
	@Column(
		name = "id"
	)
	private int id;

	@Column(
		name = "email"
	)
	private String email;

	@Column(
		name = "password"
	)
	private String password;

	public int getId() {
		return this.id;
	}

	public User setId(int id) {
		this.id = id;
		return this;
	}

	public String password() {
		return this.password;
	}

	public User setPassword(String password) {
		this.password = password;
		return this;
	}

	public String email() {
		return this.email;
	}

	public User setEmail(String email) {
		this.email = email;
		return this;
	}

}
