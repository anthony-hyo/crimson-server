package com.crimson.bakuretsu.models.avatar.character;

import com.crimson.bakuretsu.annotations.Column;
import com.crimson.bakuretsu.annotations.Id;
import com.crimson.bakuretsu.core.Model;
import com.crimson.bakuretsu.models.avatar.IAvatarData;

public class Character extends Model<Character> implements IAvatarData {

	@Id
	@Column(
		name = "id"
	)
	private int id;

	@Column(
		name = "user_id"
	)
	private int userId;

	@Column(
		name = "level_id"
	)
	private int levelId;

	@Column(
		name = "name"
	)
	private String name;

	@Column(
		name = "gender"
	)
	private String gender;

	@Column(
		name = "coins"
	)
	private int coins;

	@Column(
		name = "color_hair"
	)
	private String colorHair;

	@Column(
		name = "color_skin"
	)
	private String colorSkin;

	@Column(
		name = "color_eye"
	)
	private String colorEye;

	@Column(
		name = "slot_bag"
	)
	private int slotBag;

	@Column(
		name = "slot_bank"
	)
	private int slotBank;

	public int id() {
		return id;
	}

	@Override
	public Character setId(int id) {
		this.id = id;
		return this;
	}

	public int levelId() {
		return levelId;
	}

	public Character setLevelId(int levelId) {
		this.levelId = levelId;
		return this;
	}

	public int userId() {
		return userId;
	}

	public Character setUserId(int userId) {
		this.userId = userId;
		return this;
	}

	public String name() {
		return name;
	}

	@Override
	public Character setName(String name) {
		this.name = name;
		return this;
	}

	public String gender() {
		return gender;
	}

	public Character setGender(String gender) {
		this.gender = gender;
		return this;
	}

	public int coins() {
		return coins;
	}

	public Character setCoins(int coins) {
		this.coins = coins;
		return this;
	}

	public String colorHair() {
		return colorHair;
	}

	public Character setColorHair(String colorHair) {
		this.colorHair = colorHair;
		return this;
	}

	public String colorSkin() {
		return colorSkin;
	}

	public Character setColorSkin(String colorSkin) {
		this.colorSkin = colorSkin;
		return this;
	}

	public String colorEye() {
		return colorEye;
	}

	public Character setColorEye(String colorEye) {
		this.colorEye = colorEye;
		return this;
	}

	public int slotBag() {
		return slotBag;
	}

	public Character setSlotBag(int slotBag) {
		this.slotBag = slotBag;
		return this;
	}

	public int slotBank() {
		return slotBank;
	}

	public Character setSlotBank(int slotBank) {
		this.slotBank = slotBank;
		return this;
	}


}
