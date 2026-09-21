package com.carrot123.eternal_trinkets.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

@SuppressWarnings("null")
public class WarpedCoreBlock extends Block {

    public WarpedCoreBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(3.0F, 6.0F)
                .sound(SoundType.STEM)
                .lightLevel(state -> 11));
    }
}
