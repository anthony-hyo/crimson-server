package com.crimson.bakuretsu.models.area;

import com.crimson.bakuretsu.annotations.Cached;
import com.crimson.bakuretsu.annotations.Column;
import com.crimson.bakuretsu.annotations.Id;
import com.crimson.bakuretsu.annotations.Table;
import com.crimson.bakuretsu.core.Model;

@Cached
@Table("areas")
public class Area extends Model<Area> {

	@Id
	@Column(
		name = "id"
	)
	private int id;

	@Column(
		name = "name"
	)
	private String name;

	@Column(
		name = "asset"
	)
	private String asset;

	@Column(
		name = "music"
	)
	private String music;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String Asset() {
		return asset;
	}

	public Area setAsset(String asset) {
		this.asset = asset;
		return this;
	}

	public String Music() {
		return music;
	}

	public Area setMusic(String music) {
		this.music = music;
		return this;
	}

}
