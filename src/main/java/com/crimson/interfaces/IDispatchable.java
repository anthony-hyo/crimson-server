/*
 * Copyright (c) 2024.
 *
 * MEGUMIN (Modular Emulated Gateway for Unique and Multi-platform Infrastructure Networks)
 * is proprietary software. Redistribution and use in source or binary forms, with or without modification,
 * are prohibited without prior written permission.
 */
package com.crimson.interfaces;

import net.sf.json.JSONObject;

public interface IDispatchable {

    void dispatch(JSONObject params);

}
