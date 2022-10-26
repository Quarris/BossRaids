package dev.quarris.bossraids.util;

public class BossRaidException extends RuntimeException {

    public BossRaidException(String message) {
        super(message);
    }

    public BossRaidException(String message, Throwable cause) {
        super(message, cause);
    }
}
