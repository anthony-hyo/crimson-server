/*
 * Copyright (c) 2025.
 *
 * MEGUMIN (Modular Emulated Gateway for Unique and Multi-platform Infrastructure Networks)
 * is proprietary software. Redistribution and use in source or binary forms, with or without modification,
 * are prohibited without prior written permission.
 */
package com.crimson.exceptions;

public class EntityCreationException extends RuntimeException {
	public EntityCreationException(String message, Throwable cause) {
		super(message, cause);
	}
}
