package com.carrot123.eternal_trinkets.item;

import com.carrot123.eternal_trinkets.EternalTrinkets;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class EternalPotionPouchData {
    public static final String ROOT_TAG = "EternalPotion";
    public static final String SOURCE_POTION_TAG = "SourcePotion";
    public static final String EFFECTS_TAG = "Effects";
    public static final String EFFECT_ID_TAG = "Effect";
    public static final String AMPLIFIER_TAG = "Amplifier";
    public static final String AMBIENT_TAG = "Ambient";
    public static final String VISIBLE_TAG = "Visible";
    public static final String SHOW_ICON_TAG = "ShowIcon";

    private static final int MAX_SAFE_AMPLIFIER = 255;
    private static final Set<String> REPORTED_INVALID_ENTRIES =
            ConcurrentHashMap.newKeySet();
    private static final Set<String> REPORTED_INVALID_CONFIG_ENTRIES =
            ConcurrentHashMap.newKeySet();

    private EternalPotionPouchData() {
        throw new UnsupportedOperationException("utility class");
    }

    public static NormalizationResult normalizeEffects(
            Collection<MobEffectInstance> instances) {
        Map<ResourceLocation, StoredEffect> normalized = new LinkedHashMap<>();

        for (MobEffectInstance instance : instances) {
            MobEffect effect = instance.getEffect();
            ResourceLocation effectId = ForgeRegistries.MOB_EFFECTS.getKey(effect);
            int amplifier = instance.getAmplifier();
            if (effectId == null || amplifier < 0 || amplifier > MAX_SAFE_AMPLIFIER) {
                return new NormalizationResult(List.of(), true);
            }

            StoredEffect candidate = new StoredEffect(
                    effectId,
                    effect,
                    amplifier,
                    instance.isAmbient(),
                    instance.isVisible(),
                    instance.showIcon());
            StoredEffect existing = normalized.get(effectId);
            if (existing == null || shouldReplace(existing, candidate)) {
                normalized.put(effectId, candidate);
            }
        }
        return new NormalizationResult(List.copyOf(normalized.values()), false);
    }

    public static boolean writeStoredPotion(
            ItemStack pouchStack, Potion sourcePotion, List<StoredEffect> effects) {
        if (effects.isEmpty()) {
            return false;
        }

        try {
            CompoundTag root = new CompoundTag();
            ResourceLocation potionId = ForgeRegistries.POTIONS.getKey(sourcePotion);
            if (potionId != null) {
                root.putString(SOURCE_POTION_TAG, potionId.toString());
            }

            ListTag effectList = new ListTag();
            for (StoredEffect stored : effects) {
                ResourceLocation currentId =
                        ForgeRegistries.MOB_EFFECTS.getKey(stored.effect());
                if (currentId == null
                        || !currentId.equals(stored.effectId())
                        || stored.amplifier() < 0
                        || stored.amplifier() > MAX_SAFE_AMPLIFIER) {
                    return false;
                }

                CompoundTag effectTag = new CompoundTag();
                effectTag.putString(EFFECT_ID_TAG, stored.effectId().toString());
                effectTag.putInt(AMPLIFIER_TAG, stored.amplifier());
                effectTag.putBoolean(AMBIENT_TAG, stored.ambient());
                effectTag.putBoolean(VISIBLE_TAG, stored.visible());
                effectTag.putBoolean(SHOW_ICON_TAG, stored.showIcon());
                effectList.add(effectTag);
            }
            root.put(EFFECTS_TAG, effectList);
            pouchStack.getOrCreateTag().put(ROOT_TAG, root);
            return true;
        } catch (RuntimeException exception) {
            EternalTrinkets.LOGGER.warn(
                    "Failed to write Eternal Potion Pouch data safely", exception);
            return false;
        }
    }

    public static List<StoredEffect> readStoredEffects(ItemStack pouchStack) {
        CompoundTag itemTag = pouchStack.getTag();
        if (itemTag == null || !itemTag.contains(ROOT_TAG, Tag.TAG_COMPOUND)) {
            return List.of();
        }

        CompoundTag root = itemTag.getCompound(ROOT_TAG);
        if (!root.contains(EFFECTS_TAG, Tag.TAG_LIST)) {
            reportInvalidOnce("missing-effects-list");
            return List.of();
        }

        ListTag effectTags = root.getList(EFFECTS_TAG, Tag.TAG_COMPOUND);
        Map<ResourceLocation, StoredEffect> effects = new LinkedHashMap<>();
        for (int index = 0; index < effectTags.size(); index++) {
            CompoundTag effectTag = effectTags.getCompound(index);
            String rawId = effectTag.getString(EFFECT_ID_TAG);
            ResourceLocation effectId = ResourceLocation.tryParse(rawId);
            int amplifier = effectTag.getInt(AMPLIFIER_TAG);
            if (effectId == null
                    || amplifier < 0
                    || amplifier > MAX_SAFE_AMPLIFIER) {
                reportInvalidOnce(rawId + "#" + amplifier);
                continue;
            }

            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(effectId);
            if (effect == null) {
                reportInvalidOnce(effectId.toString());
                continue;
            }

            StoredEffect candidate = new StoredEffect(
                    effectId,
                    effect,
                    amplifier,
                    effectTag.getBoolean(AMBIENT_TAG),
                    effectTag.getBoolean(VISIBLE_TAG),
                    effectTag.getBoolean(SHOW_ICON_TAG));
            StoredEffect existing = effects.get(effectId);
            if (existing == null || shouldReplace(existing, candidate)) {
                effects.put(effectId, candidate);
            }
        }
        return List.copyOf(effects.values());
    }

    public static boolean hasStoredEffects(ItemStack pouchStack) {
        return !readStoredEffects(pouchStack).isEmpty();
    }

    public static void clearStoredPotion(ItemStack pouchStack) {
        CompoundTag itemTag = pouchStack.getTag();
        if (itemTag != null) {
            itemTag.remove(ROOT_TAG);
        }
    }

    public static boolean areEffectsAllowed(
            Collection<StoredEffect> effects,
            boolean whitelist,
            Set<ResourceLocation> configuredEffects) {
        if (effects.isEmpty()) {
            return false;
        }

        if (whitelist) {
            return effects.stream()
                    .allMatch(effect ->
                            configuredEffects.contains(effect.effectId()));
        }

        return effects.stream()
                .noneMatch(effect ->
                        configuredEffects.contains(effect.effectId()));
    }

    public static Set<ResourceLocation> parseConfiguredEffectIds(
            Collection<? extends String> rawEntries) {
        Set<ResourceLocation> parsed = new LinkedHashSet<>();
        for (String rawEntry : rawEntries) {
            String normalized = rawEntry == null
                    ? ""
                    : rawEntry.trim().toLowerCase(Locale.ROOT);
            ResourceLocation effectId = ResourceLocation.tryParse(normalized);
            if (effectId == null) {
                if (REPORTED_INVALID_CONFIG_ENTRIES.add(String.valueOf(rawEntry))) {
                    EternalTrinkets.LOGGER.warn(
                            "Ignoring invalid MobEffect ID in Eternal Potion Pouch item config: {}",
                            rawEntry);
                }
                continue;
            }
            parsed.add(effectId);
        }
        return Collections.unmodifiableSet(parsed);
    }

    public static List<ResourceLocation> getStoredEffectIds(ItemStack pouchStack) {
        List<ResourceLocation> result = new ArrayList<>();
        for (StoredEffect stored : readStoredEffects(pouchStack)) {
            result.add(stored.effectId());
        }
        return List.copyOf(result);
    }

    private static boolean shouldReplace(
            StoredEffect existing, StoredEffect candidate) {
        if (candidate.amplifier() != existing.amplifier()) {
            return candidate.amplifier() > existing.amplifier();
        }
        return displayCompleteness(candidate) > displayCompleteness(existing);
    }

    private static int displayCompleteness(StoredEffect effect) {
        return (effect.visible() ? 2 : 0) + (effect.showIcon() ? 1 : 0);
    }

    private static void reportInvalidOnce(String entry) {
        if (REPORTED_INVALID_ENTRIES.add(entry)) {
            EternalTrinkets.LOGGER.debug(
                    "Ignoring invalid Eternal Potion Pouch effect entry: {}", entry);
        }
    }

    public record StoredEffect(
            ResourceLocation effectId,
            MobEffect effect,
            int amplifier,
            boolean ambient,
            boolean visible,
            boolean showIcon) {
    }

    public record NormalizationResult(
            List<StoredEffect> effects,
            boolean invalid) {
    }
}
