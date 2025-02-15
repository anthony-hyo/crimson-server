package com.crimson.bakuretsu.models.area;

import com.crimson.bakuretsu.annotations.Cached;
import com.crimson.bakuretsu.annotations.Column;
import com.crimson.bakuretsu.annotations.Id;
import com.crimson.bakuretsu.annotations.Table;
import com.crimson.bakuretsu.core.Model;

@Cached
@Table("areas_frames")
public class AreaFrame extends Model<Area> {

	@Id
	@Column(
		name = "id"
	)
	private int id;

	@Column(
		name = "Name"
	)
	private String Name;

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

}
