package com.crimson.avatar;

import com.crimson.avatar.player.Player;

public abstract class Avatar<D extends AvatarData> {

    public abstract D data();

    public boolean doesNotExist() {
        return this.equals(Player.NONE);
    }

    public boolean doesExist() {
        return !doesNotExist();
    }

}
