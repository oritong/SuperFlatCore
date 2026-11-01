package com.oritong.superflatcore.integration.botania;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.oritong.superflatcore.SuperFlatCore;
import com.oritong.superflatcore.machine.ManaFluidHatchPartMachine;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.mana.ManaReceiver;

public final class ManaSparkHatchIntegration {
    private ManaSparkHatchIntegration() {}

    public static void register() {
        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class,
                ManaSparkHatchIntegration::attachCapabilities);
    }

    private static void attachCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
        if (!(event.getObject() instanceof IMachineBlockEntity machineBlockEntity)
                || !(machineBlockEntity.getMetaMachine() instanceof ManaFluidHatchPartMachine hatch)
                || !hatch.isInputHatch()) {
            return;
        }
        ManaReceiverCapability provider = new ManaReceiverCapability(hatch);
        event.addCapability(SuperFlatCore.id("mana_spark_receiver"), provider);
        event.addListener(provider::invalidate);
    }

    private static final class ManaReceiverCapability implements ICapabilityProvider {
        private final LazyOptional<ManaReceiver> manaReceiver;

        private ManaReceiverCapability(ManaFluidHatchPartMachine hatch) {
            manaReceiver = LazyOptional.of(() -> hatch);
        }

        @NotNull
        @Override
        public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability,
                                                @Nullable Direction direction) {
            if (capability == BotaniaForgeCapabilities.MANA_RECEIVER) {
                return manaReceiver.cast();
            }
            return LazyOptional.empty();
        }

        private void invalidate() {
            manaReceiver.invalidate();
        }
    }
}
