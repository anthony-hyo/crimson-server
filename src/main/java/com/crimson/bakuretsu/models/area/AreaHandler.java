package com.crimson.bakuretsu.models.area;

import com.crimson.bakuretsu.annotations.Cached;
import com.crimson.bakuretsu.annotations.Column;
import com.crimson.bakuretsu.annotations.Id;
import com.crimson.bakuretsu.annotations.Table;
import com.crimson.bakuretsu.core.Model;

@Cached
@Table("areas_handlers")
public class AreaHandler extends Model<AreaHandler> {

	@Id
	@Column(
		name = "id"
	)
	private int id;

	@Column(
		name = "area_id"
	)
	private int areaId;

	@Column(
		name = "handler"
	)
	private String handler ;

	@Column(
		name = "parameter"
	)
	private String parameter; //TODO: Change to JSON ELEMENT

	@Column(
		name = "order"
	)
	private int order;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int mapId() {
		return areaId;
	}

	public AreaHandler setAreaId(int areaId) {
		this.areaId = areaId;
		return this;
	}

	public String handler() {
		return handler;
	}

	public AreaHandler setHandler(String handler) {
		this.handler = handler;
		return this;
	}

	public String parameter() {
		return parameter;
	}

	public AreaHandler setParameter(String parameter) {
		this.parameter = parameter;
		return this;
	}

	public int order() {
		return order;
	}

	public AreaHandler setOrder(int order) {
		this.order = order;
		return this;
	}

}
