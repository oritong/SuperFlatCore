package com.oritong.superflatcore.mixin;

import com.gregtechceu.gtceu.api.capability.compat.EUToFEProvider;
import com.gregtechceu.gtceu.api.capability.compat.FeCompat;
import net.minecraft.core.Direction;
import net.minecraftforge.energy.IEnergyStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = EUToFEProvider.GTEnergyWrapper.class, remap = false)
public abstract class EUToFEBufferedOutputMixin {
    @Redirect(method = "acceptEnergyFromNetwork", at = @At(value = "INVOKE",
            target = "Lnet/minecraftforge/energy/IEnergyStorage;receiveEnergy(IZ)I", ordinal = 0))
    private int superflatcore$probeBufferWithIncomingPackets(IEnergyStorage storage, int bufferedFe, boolean simulate,
                                                           Direction facing, long voltage, long amperage) {
        // A remainder smaller than the destination's minimum packet cannot be
        // sent alone. Include the incoming EU packets in the initial probe so
        // GT can use its existing accounting to send both together.
        long packetFe = FeCompat.toFeBounded(voltage, FeCompat.ratio(false), Integer.MAX_VALUE);
        long incomingFe = packetFe * Math.min(amperage, Integer.MAX_VALUE);
        int combinedFe = (int) (bufferedFe + Math.min(Integer.MAX_VALUE - (long) bufferedFe, incomingFe));
        return storage.receiveEnergy(combinedFe, simulate);
    }
}
