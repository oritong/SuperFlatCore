package com.oritong.superflatcore.mixin;

import com.oritong.superflatcore.world.DiamondContinentWorld;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerSpawnMixin {
    @Inject(method = "fudgeSpawnLocation", at = @At("HEAD"), cancellable = true)
    private void superflatcore$spawnInsideStarterHouse(ServerLevel level, CallbackInfo ci) {
        if (!DiamondContinentWorld.hasTemplateTerrain(level)) {
            return;
        }
        ServerPlayer player = (ServerPlayer) (Object) this;
        var spawn = level.getSharedSpawnPos();
        // The template supplies its exact spawn. Neither center it nor run vanilla's
        // randomized surface search, which can place the player on the house's roof.
        player.moveTo(spawn.getX(), spawn.getY(), spawn.getZ(), level.getSharedSpawnAngle(), 0);
        ci.cancel();
    }
}
