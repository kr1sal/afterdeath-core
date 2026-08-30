package com.afterdeath.core.phylactery;

import com.afterdeath.core.AfterdeathCore;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class PhylacteryComponents {
    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, AfterdeathCore.MODID);

    public static final Supplier<DataComponentType<Integer>> CHARGE = COMPONENTS.registerComponentType(
            "phylactery_charge",
            builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
    );

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
    }

    private PhylacteryComponents() {}
}
