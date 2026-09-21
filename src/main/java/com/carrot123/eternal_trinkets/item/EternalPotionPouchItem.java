package com.carrot123.eternal_trinkets.item;

import com.carrot123.eternal_trinkets.EternalTrinkets;
import com.carrot123.eternal_trinkets.item.EternalPotionPouchData.NormalizationResult;
import com.carrot123.eternal_trinkets.item.EternalPotionPouchData.StoredEffect;
import com.carrot123.eternal_trinkets.misc.ModConfig;
import com.carrot123.eternal_trinkets.network.ModNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class EternalPotionPouchItem extends Item {
    public static final int REFRESH_INTERVAL_TICKS = 20 * 5;
    public static final int EFFECT_DURATION_TICKS = 20 * 20;

    private static final AtomicBoolean REPORTED_UNLOADED_ITEM_CONFIG =
            new AtomicBoolean();

    public EternalPotionPouchItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    }

    @Override
    public boolean overrideOtherStackedOnMe(
            ItemStack pouchStack,
            ItemStack carriedStack,
            Slot pouchSlot,
            ClickAction action,
            Player player,
            SlotAccess carriedSlotAccess) {
        if (action != ClickAction.SECONDARY
                || !pouchStack.is(ModItems.ETERNAL_POTION_POUCH.get())
                || !carriedStack.is(Items.POTION)) {
            return false;
        }

        if (player.level().isClientSide) {
            if (player.getAbilities().instabuild) {
                int inventorySlot = pouchSlot.getContainerSlot();
                ItemStack potionCopy = carriedStack.copy();
                DistExecutor.unsafeRunWhenOn(
                        Dist.CLIENT,
                        () -> () -> ModNetwork.requestCreativePouchStore(
                                inventorySlot, potionCopy));
            }
            return true;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }

        if (!tryStorePotion(serverPlayer, pouchStack, carriedStack)) {
            return true;
        }

        replaceConsumedPotion(serverPlayer, carriedStack, carriedSlotAccess);
        pouchSlot.setChanged();
        return true;
    }

    public boolean storeCreativePotion(
            ServerPlayer player,
            ItemStack pouchStack,
            ItemStack potionStack) {
        if (!player.gameMode.isCreative()
                || !pouchStack.is(ModItems.ETERNAL_POTION_POUCH.get())
                || !potionStack.is(Items.POTION)) {
            return false;
        }

        boolean stored = tryStorePotion(player, pouchStack, potionStack);
        if (stored) {
            player.inventoryMenu.broadcastChanges();
        }
        return stored;
    }

    private boolean tryStorePotion(
            ServerPlayer serverPlayer,
            ItemStack pouchStack,
            ItemStack potionStack) {
        List<MobEffectInstance> potionEffects =
                PotionUtils.getMobEffects(potionStack);
        if (potionEffects.isEmpty()) {
            sendFeedback(serverPlayer, "empty_potion");
            return false;
        }

        NormalizationResult normalized =
                EternalPotionPouchData.normalizeEffects(potionEffects);
        if (normalized.invalid()) {
            sendFeedback(serverPlayer, "invalid_effect");
            return false;
        }
        if (!isAllowedByServerConfig(normalized.effects())) {
            sendFeedback(serverPlayer, "filtered");
            return false;
        }

        List<StoredEffect> previous =
                EternalPotionPouchData.readStoredEffects(pouchStack);
        if (!EternalPotionPouchData.writeStoredPotion(
                pouchStack, PotionUtils.getPotion(potionStack), normalized.effects())) {
            sendFeedback(serverPlayer, "invalid_effect");
            return false;
        }

        for (StoredEffect oldEffect : previous) {
            serverPlayer.removeEffect(oldEffect.effect());
        }
        applyStoredEffects(serverPlayer, pouchStack);
        refreshOtherPouchEffects(serverPlayer, pouchStack);
        serverPlayer.getCooldowns().addCooldown(this, 2);
        sendFeedback(serverPlayer, "stored");
        return true;
    }

    public static ItemStack creativeCarriedResult(
            ItemStack potionStack, boolean stored) {
        return stored
                ? new ItemStack(Items.GLASS_BOTTLE)
                : potionStack.copy();
    }

    @Override
    public void inventoryTick(
            ItemStack stack,
            Level level,
            Entity entity,
            int slotIndex,
            boolean selected) {
        if (!level.isClientSide
                && entity instanceof ServerPlayer player
                && player.tickCount % REFRESH_INTERVAL_TICKS == 0
                && !player.getCooldowns().isOnCooldown(this)
                && EternalPotionPouchData.hasStoredEffects(stack)) {
            applyStoredEffects(player, stack);
        }
    }

    public static void applyStoredEffects(
            ServerPlayer player, ItemStack pouchStack) {
        applyStoredEffects(player, pouchStack, true);
    }

    private static void applyStoredEffects(
            ServerPlayer player,
            ItemStack pouchStack,
            boolean includeInstantEffects) {
        for (StoredEffect stored :
                EternalPotionPouchData.readStoredEffects(pouchStack)) {
            if (stored.effect().isInstantenous()) {
                if (includeInstantEffects) {
                    stored.effect().applyInstantenousEffect(
                            player, player, player, stored.amplifier(), 1.0D);
                }
            } else {
                player.addEffect(new MobEffectInstance(
                        stored.effect(),
                        EFFECT_DURATION_TICKS,
                        stored.amplifier(),
                        stored.ambient(),
                        stored.visible(),
                        stored.showIcon()));
            }
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return EternalPotionPouchData.hasStoredEffects(stack)
                || super.isFoil(stack);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag) {
        List<StoredEffect> effects =
                EternalPotionPouchData.readStoredEffects(stack);
        if (effects.isEmpty()) {
            tooltip.add(Component.translatable(
                            "tooltip.eternal_trinkets.eternal_potion_pouch.empty")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        tooltip.add(Component.translatable(
                        "tooltip.eternal_trinkets.eternal_potion_pouch.stored")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        boolean hasInstantEffect = false;
        for (StoredEffect stored : effects) {
            Component levelText = createLevelText(stored.amplifier());
            tooltip.add(Component.translatable(stored.effect().getDescriptionId())
                    .append(" ")
                    .append(levelText)
                    .withStyle(ChatFormatting.BLUE));
            hasInstantEffect |= stored.effect().isInstantenous();
        }
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable(
                        "tooltip.eternal_trinkets.eternal_potion_pouch.refresh")
                .withStyle(ChatFormatting.GRAY));
        if (hasInstantEffect) {
            tooltip.add(Component.translatable(
                            "tooltip.eternal_trinkets.eternal_potion_pouch.instant")
                    .withStyle(ChatFormatting.GOLD));
        }
    }

    private static boolean isAllowedByServerConfig(List<StoredEffect> effects) {
        boolean whitelist = ModConfig.DEFAULT_ETERNAL_POTION_POUCH_WHITELIST;
        List<? extends String> rawEntries =
                ModConfig.DEFAULT_ETERNAL_POTION_POUCH_EFFECT_LIST;

        if (!ModConfig.ITEM_CONFIG.isLoaded()) {
            if (REPORTED_UNLOADED_ITEM_CONFIG.compareAndSet(false, true)) {
                EternalTrinkets.LOGGER.error(
                        "Eternal Potion Pouch attempted to read item.toml before "
                                + "ITEM_CONFIG was loaded; using the default blacklist");
            }
        } else {
            try {
                whitelist = ModConfig.ETERNAL_POTION_POUCH_WHITELIST.get();
                rawEntries = ModConfig.ETERNAL_POTION_POUCH_EFFECT_LIST.get();
            } catch (RuntimeException exception) {
                EternalTrinkets.LOGGER.error(
                        "Failed to read Eternal Potion Pouch settings from item.toml",
                        exception);
                return false;
            }
        }

        Set<ResourceLocation> configured =
                EternalPotionPouchData.parseConfiguredEffectIds(rawEntries);
        boolean allowed = EternalPotionPouchData.areEffectsAllowed(
                effects, whitelist, configured);
        List<ResourceLocation> effectIds = effects.stream()
                .map(StoredEffect::effectId)
                .toList();
        String mode = whitelist ? "WHITELIST" : "BLACKLIST";
        EternalTrinkets.LOGGER.debug(
                "Potion pouch filter: mode={} effects={} configured={} allowed={}",
                mode,
                effectIds,
                configured,
                allowed);
        if (!allowed) {
            boolean whitelistMode = whitelist;
            ResourceLocation matched = effects.stream()
                    .map(StoredEffect::effectId)
                    .filter(effectId ->
                            whitelistMode != configured.contains(effectId))
                    .findFirst()
                    .orElse(null);
            EternalTrinkets.LOGGER.debug(
                    "Potion pouch rejected: effects={} matched={} mode={}",
                    effectIds,
                    matched,
                    mode);
        }
        return allowed;
    }

    private static void replaceConsumedPotion(
            ServerPlayer player,
            ItemStack carriedStack,
            SlotAccess carriedSlotAccess) {
        if (carriedStack.getCount() <= 1) {
            carriedSlotAccess.set(new ItemStack(Items.GLASS_BOTTLE));
            return;
        }

        ItemStack remaining = carriedStack.copy();
        remaining.shrink(1);
        carriedSlotAccess.set(remaining);
        ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
        if (!player.getInventory().add(bottle)) {
            player.drop(bottle, false);
        }
    }

    private static void refreshOtherPouchEffects(
            ServerPlayer player, ItemStack excludedPouch) {
        for (int index = 0;
             index < player.getInventory().getContainerSize();
             index++) {
            ItemStack candidate = player.getInventory().getItem(index);
            if (candidate != excludedPouch
                    && candidate.is(ModItems.ETERNAL_POTION_POUCH.get())
                    && EternalPotionPouchData.hasStoredEffects(candidate)) {
                applyStoredEffects(player, candidate, false);
            }
        }
    }

    private static Component createLevelText(int amplifier) {
        if (amplifier >= 0 && amplifier <= 4) {
            return Component.translatable("potion.potency." + amplifier);
        }
        return Component.literal(Integer.toString(amplifier + 1));
    }

    private static void sendFeedback(ServerPlayer player, String result) {
        player.displayClientMessage(Component.translatable(
                "message.eternal_trinkets.eternal_potion_pouch." + result), true);
    }
}
