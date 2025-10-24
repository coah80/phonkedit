package com.phonkedit.state;

public final class ServerFreezeState {
    private static volatile long worldFrozenUntilMillis = 0L;
    private static volatile long damageProtectedUntilMillis = 0L;

    private ServerFreezeState() {}

    public static void freezeWorldForMillis(long durationMs) {
        long now = System.currentTimeMillis();
        long until = now + Math.max(0L, durationMs);
        if (until > worldFrozenUntilMillis) {
            worldFrozenUntilMillis = until;
        }
    }

    public static void protectDamageForMillis(long durationMs) {
        long now = System.currentTimeMillis();
        long until = now + Math.max(0L, durationMs);
        if (until > damageProtectedUntilMillis) {
            damageProtectedUntilMillis = until;
        }
    }

    public static boolean isWorldFrozen() {
        return System.currentTimeMillis() < worldFrozenUntilMillis;
    }

    public static boolean isDamageProtected() {
        return System.currentTimeMillis() < damageProtectedUntilMillis;
    }

    public static void endProtectionNow() {
        worldFrozenUntilMillis = 0L;
        damageProtectedUntilMillis = 0L;
    }
}
