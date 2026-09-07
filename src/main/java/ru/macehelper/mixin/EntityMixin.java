package ru.macehelper.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.macehelper.client.MaceHelperClient;

@Mixin(LivingEntity.class)
public abstract class EntityMixin {
    @Inject(method = "handleDamageEvent", at = @At("TAIL"))
    private void maceHelper$onClientDamageEvent(DamageSource source, CallbackInfo callback) {
        Entity attacker = source.getEntity();
        ItemStack weapon = source.getWeaponItem();
        if (attacker instanceof Player player) {
            MaceHelperClient.onObservedMaceHit(player, weapon);
        }
    }
}
