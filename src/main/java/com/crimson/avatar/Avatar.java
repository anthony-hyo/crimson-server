package com.crimson.avatar;

import com.crimson.avatar.player.Player;
import com.crimson.bakuretsu.models.avatar.IAvatarData;

public abstract class Avatar<D extends IAvatarData> {

    public abstract D data();

    public boolean doesNotExist() {
        return this.equals(Avatar.NONE);
    }

    public boolean doesExist() {
        return !doesNotExist();
    }

}
