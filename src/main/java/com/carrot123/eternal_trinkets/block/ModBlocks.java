package com.carrot123.eternal_trinkets.block;

import com.carrot123.eternal_trinkets.EternalTrinkets;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("null")
public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, EternalTrinkets.MODID);

    public static final RegistryObject<Block> WARPED_CORE =
            BLOCKS.register("warped_core", WarpedCoreBlock::new);

    private ModBlocks() {
        throw new UnsupportedOperationException("utility class");
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
