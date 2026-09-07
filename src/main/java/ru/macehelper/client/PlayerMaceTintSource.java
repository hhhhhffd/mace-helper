package ru.macehelper.client;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class PlayerMaceTintSource implements ItemTintSource {
    public static final MapCodec<PlayerMaceTintSource> CODEC = MapCodec.unit(new PlayerMaceTintSource());

    private PlayerMaceTintSource() {
    }

    @Override
    public int calculate(ItemStack stack, ClientLevel level, LivingEntity entity) {
        return MaceHelperClient.getTintColor(entity);
    }

    @Override
    public MapCodec<PlayerMaceTintSource> type() {
        return CODEC;
    }
}
