package com.carrot123.eternal_trinkets.event;

import com.carrot123.eternal_trinkets.block.ModBlocks;
import com.carrot123.eternal_trinkets.entity.neutral.WarpedFungusSprite;
import com.carrot123.eternal_trinkets.entity.neutral.WarpedFungusUmbrella;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WarpedCoreEvents {

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (event.getState().is(ModBlocks.WARPED_CORE.get())) {
            Player player = event.getPlayer();
            Level level = (Level) event.getLevel();
            WarpedFungusSprite.angerNearbySprites(player, level, event.getPos());
            WarpedFungusUmbrella.angerNearbyUmbrellas(player, level, event.getPos());
        }
    }
}
