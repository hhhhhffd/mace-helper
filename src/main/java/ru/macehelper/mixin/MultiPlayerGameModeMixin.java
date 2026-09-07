package ru.macehelper.mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.macehelper.client.MaceHelperClient;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void maceHelper$onAttack(Player player, Entity target, CallbackInfo callback) {
        if (MaceHelperClient.shouldBlockMaceAttack(player)) {
            callback.cancel();
            return;
        }

        MaceHelperClient.onLocalMaceAttack(player);
    }
}
