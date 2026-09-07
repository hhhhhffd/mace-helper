package ru.macehelper.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MaceHelperClient implements ClientModInitializer {
    public static final String MOD_ID = "mace_helper";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final int NO_TINT = 0xFFFFFFFF;

    private static MaceHelperConfig config;
    private static final PlayerColorTracker COLOR_TRACKER = new PlayerColorTracker();

    @Override
    public void onInitializeClient() {
        config = MaceHelperConfig.load();

        ItemTintSources.ID_MAPPER.put(
                Identifier.fromNamespaceAndPath(MOD_ID, "player_color"),
                PlayerMaceTintSource.CODEC
        );

        ClientPreAttackCallback.EVENT.register(MaceHelperClient::onPreAttack);
        ClientTickEvents.END_CLIENT_TICK.register(MaceHelperClient::onEndClientTick);
    }

    private static boolean onPreAttack(Minecraft client, LocalPlayer player, int clickCount) {
        return shouldBlockMaceAttack(player);
    }

    private static void onEndClientTick(Minecraft client) {
        COLOR_TRACKER.tick(client, getConfig());
    }

    public static MaceHelperConfig getConfig() {
        if (config == null) {
            config = MaceHelperConfig.load();
        }
        return config;
    }

    public static void onLocalMaceAttack(Player attacker) {
        MaceHelperConfig currentConfig = getConfig();
        Minecraft client = Minecraft.getInstance();
        if (!currentConfig.isEnabled() || client.player != attacker || !attacker.getMainHandItem().is(Items.MACE)) {
            return;
        }

        registerMaceHit(attacker, true, currentConfig);
    }

    public static boolean shouldBlockMaceAttack(Player attacker) {
        MaceHelperConfig currentConfig = getConfig();
        return currentConfig.isEnabled()
                && Minecraft.getInstance().player == attacker
                && attacker.getMainHandItem().is(Items.MACE)
                && COLOR_TRACKER.isAttackBlocked(attacker.getUUID());
    }

    public static void onObservedMaceHit(Player attacker, ItemStack weapon) {
        MaceHelperConfig currentConfig = getConfig();
        if (!currentConfig.isEnabled() || weapon == null || !weapon.is(Items.MACE)) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        registerMaceHit(attacker, client.player == attacker, currentConfig);
    }

    private static void registerMaceHit(Player attacker, boolean localPlayer, MaceHelperConfig currentConfig) {
        long gameTime = attacker.level().getGameTime();
        COLOR_TRACKER.onMaceHit(attacker, localPlayer, gameTime, currentConfig);
    }

    public static int getTintColor(LivingEntity holder) {
        MaceHelperConfig currentConfig = getConfig();
        if (!currentConfig.isEnabled()) {
            return NO_TINT;
        }

        Minecraft client = Minecraft.getInstance();
        Player player = holder instanceof Player holderPlayer ? holderPlayer : client.player;
        if (player == null) {
            return NO_TINT;
        }

        return COLOR_TRACKER.getColor(player.getUUID(), currentConfig);
    }

    public static void onConfigChanged() {
        COLOR_TRACKER.reset();
    }
}
