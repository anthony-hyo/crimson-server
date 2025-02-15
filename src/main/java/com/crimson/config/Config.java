/*
 * Copyright (c) 2024-2025.
 *
 * MEGUMIN (Modular Emulated Gateway for Unique and Multi-platform Infrastructure Networks)
 * is proprietary software. Redistribution and use in source or binary forms, with or without modification,
 * are prohibited without prior written permission.
 */
package com.crimson.config;

import com.crimson.config.data.DatabaseData;
import com.crimson.config.data.GameData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;

public record Config(int id, String name, GameData game, DatabaseData database) {

	private static final Config data;

	static {
		try {
			data = new ObjectMapper(new YAMLFactory())
				.readValue(
					new File(String.format("%s%sconf%s%s", (new File(".").getCanonicalPath()), File.separatorChar, File.separatorChar, "config.yml")),
					Config.class
				);
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	public static Config singleton() {
		return data;
	}

}


