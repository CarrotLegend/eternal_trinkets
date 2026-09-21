package com.carrot123.eternal_trinkets.event;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.carrot123.eternal_trinkets.item.curio.ModCurioItems;
import com.carrot123.eternal_trinkets.misc.ModConfig;
import com.carrot123.eternal_trinkets.util.CuriosUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 超幸运的四叶草！
 */
public class LuckyCloverEvents {

    private final Map<UUID, TempFortune> activeFortune = new HashMap<>();

    private record TempFortune(ItemStack tool, Map<Enchantment, Integer> original) {}

    @SuppressWarnings("null")
    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Player player = event.getPlayer();
        if (player == null || player.isCreative() || player.isSpectator()) {
            return;
        }
        BlockState state = event.getState();
        BlockPos pos = event.getPos();

        // 破坏草类 → 概率掉落四叶草
        if (isGrassLike(state)) {
            double dropChance = ModConfig.CLOVER_DROP_CHANCE.get();
            if (dropChance > 0 && level.random.nextDouble() < dropChance) {
                Block.popResource(level, pos, new ItemStack(ModCurioItems.LUCKY_CLOVER.get()));
            }
        }

        if (!CuriosUtils.hasCurioEquipped(player, ModCurioItems.LUCKY_CLOVER.get())) {
            return;
        }
        if (state.is(Tags.Blocks.STONE)) {
            if (level.random.nextDouble() < ModConfig.CLOVER_EMERALD_CHANCE.get()) {
                Block.popResource(level, pos, new ItemStack(Items.EMERALD));
            }
            if (level.random.nextDouble() < ModConfig.CLOVER_DIAMOND_CHANCE.get()) {
                Block.popResource(level, pos, new ItemStack(Items.DIAMOND));
            }
        }
        int fortuneBonus = ModConfig.CLOVER_FORTUNE.get();
        if (fortuneBonus > 0) {
            applyTemporaryFortune(level, player, fortuneBonus);
        }
    }

    @SubscribeEvent
    public void onLootingLevel(LootingLevelEvent event) {
        if (event.getDamageSource() == null) {
            return;
        }
        if (event.getDamageSource().getEntity() instanceof Player player
                && CuriosUtils.hasCurioEquipped(player, ModCurioItems.LUCKY_CLOVER.get())) {
            event.setLootingLevel(event.getLootingLevel() + ModConfig.CLOVER_LOOTING.get());
        }
    }

    private void applyTemporaryFortune(ServerLevel level, Player player, int bonus) {
        UUID id = player.getUUID();
        if (activeFortune.containsKey(id)) {
            return;
        }
        ItemStack tool = player.getMainHandItem();
        if (tool.isEmpty()) {
            return;
        }
        Map<Enchantment, Integer> original = EnchantmentHelper.getEnchantments(tool);
        Map<Enchantment, Integer> boosted = new HashMap<>(original);
        boosted.merge(Enchantments.BLOCK_FORTUNE, bonus, Integer::sum);
        EnchantmentHelper.setEnchantments(boosted, tool);

        activeFortune.put(id, new TempFortune(tool, original));
        level.getServer().execute(() -> revertFortune(id));
    }

    private void revertFortune(UUID id) {
        TempFortune tf = activeFortune.remove(id);
        if (tf == null) {
            return;
        }
        ItemStack tool = tf.tool();
        if (tool.isEmpty()) {
            return;
        }
        if (tf.original().isEmpty()) {
            CompoundTag tag = tool.getTag();
            if (tag != null) {
                tag.remove("Enchantments");
                if (tag.isEmpty()) {
                    tool.setTag(null);
                }
            }
        } else {
            EnchantmentHelper.setEnchantments(tf.original(), tool);
        }
    }

    private static boolean isGrassLike(BlockState state) {
        return state.is(Blocks.GRASS)
                || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.FERN)
                || state.is(Blocks.LARGE_FERN);
    }
}
