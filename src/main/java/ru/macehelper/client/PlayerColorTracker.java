package ru.macehelper.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

final class PlayerColorTracker {
    private final Map<UUID, PlayerColorState> states = new HashMap<>();
    private final Map<UUID, Long> lastHitGameTimes = new HashMap<>();
    private ClientLevel currentLevel;

    void onMaceHit(Player attacker, boolean localPlayer, long gameTime, MaceHelperConfig config) {
        UUID playerId = attacker.getUUID();
        Long previousHitTime = lastHitGameTimes.put(playerId, gameTime);
        if (previousHitTime != null && previousHitTime == gameTime) {
            return;
        }

        PlayerColorState state = states.computeIfAbsent(
                playerId,
                ignored -> new PlayerColorState(config.getFirstColor())
        );
        state.onHit(
                config.getFirstColor(),
                config.getSecondColor(),
                localPlayer,
                System.nanoTime(),
                durationNanos(config.getDurationSeconds())
        );
    }

    boolean isAttackBlocked(UUID playerId) {
        PlayerColorState state = states.get(playerId);
        return state != null && state.isTransitioning(System.nanoTime());
    }

    int getColor(UUID playerId, MaceHelperConfig config) {
        PlayerColorState state = states.computeIfAbsent(
                playerId,
                ignored -> new PlayerColorState(config.getFirstColor())
        );
        return state.getColor(System.nanoTime());
    }

    void tick(Minecraft client, MaceHelperConfig config) {
        ClientLevel level = client.level;
        if (level != currentLevel) {
            currentLevel = level;
            reset();
        }

        if (!config.isEnabled() || level == null) {
            reset();
            return;
        }

        long now = System.nanoTime();
        states.values().forEach(state -> state.getColor(now));

        Set<UUID> visiblePlayers = new HashSet<>();
        for (Player player : level.players()) {
            visiblePlayers.add(player.getUUID());
        }
        states.keySet().removeIf(playerId -> !visiblePlayers.contains(playerId));
        lastHitGameTimes.keySet().removeIf(playerId -> !visiblePlayers.contains(playerId));
    }

    void reset() {
        states.clear();
        lastHitGameTimes.clear();
    }

    private static long durationNanos(double seconds) {
        return Math.max(1L, Math.round(seconds * 1_000_000_000L));
    }
}
