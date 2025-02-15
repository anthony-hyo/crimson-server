/*
 * Copyright (c) 2024.
 *
 * MEGUMIN (Modular Emulated Gateway for Unique and Multi-platform Infrastructure Networks)
 * is proprietary software. Redistribution and use in source or binary forms, with or without modification,
 * are prohibited without prior written permission.
 */
package com.crimson.config.data;

public record DatabaseData(String host, int port, String user, String password, String database) {

    public String JDBC_URL() {
        return "jdbc:mariadb://%s:%s/%s".formatted(host(), port(), database());
    }

}
