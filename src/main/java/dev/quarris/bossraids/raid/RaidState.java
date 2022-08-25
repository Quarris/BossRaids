package dev.quarris.bossraids.raid;

import net.minecraft.util.IStringSerializable;

import java.util.Locale;

public enum RaidState implements IStringSerializable {
    INACTIVE, IDLE, AWAITING, CHARGING, IN_PROGRESS, COMPLETED;

    public boolean inactive() {
        return this == INACTIVE;
    }

    public boolean inProgress() {
        return this == IN_PROGRESS;
    }

    public boolean idle() {
        return this == IDLE;
    }

    public boolean awaiting() {
        return this == AWAITING;
    }

    public boolean charging() {
        return this == CHARGING;
    }

    public boolean completed() {
        return this == COMPLETED;
    }

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
