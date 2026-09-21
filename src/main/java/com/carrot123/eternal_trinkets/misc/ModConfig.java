package com.carrot123.eternal_trinkets.misc;

import com.carrot123.eternal_trinkets.EternalTrinkets;
import com.carrot123.eternal_trinkets.item.EternalPotionPouchData;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.List;
import java.util.Set;

public final class ModConfig {

    // ── mob.toml ─────────────────────────────────────────────────
    public static final ForgeConfigSpec MOB_CONFIG;

    // 诡异菌精灵
    public static final ForgeConfigSpec.DoubleValue WARPED_FUNGUS_SPRITE_ATTACK_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue WARPED_FUNGUS_SPRITE_MAX_HEALTH;
    public static final ForgeConfigSpec.DoubleValue WARPED_FUNGUS_SPRITE_ARMOR;

    // 诡异菌伞
    public static final ForgeConfigSpec.DoubleValue WARPED_FUNGUS_UMBRELLA_COLLISION_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue WARPED_FUNGUS_UMBRELLA_SPORE_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue WARPED_FUNGUS_UMBRELLA_MAX_HEALTH;
    public static final ForgeConfigSpec.DoubleValue WARPED_FUNGUS_UMBRELLA_ARMOR;

    // ── item.toml ────────────────────────────────────────────────
    // Project rule: all item-related settings belong to this builder/spec.
    // Give each item its own section; do not create per-item config files.
    public static final ForgeConfigSpec ITEM_CONFIG;

    public static final boolean DEFAULT_ETERNAL_POTION_POUCH_WHITELIST = false;
    public static final List<String> DEFAULT_ETERNAL_POTION_POUCH_EFFECT_LIST =
            List.of(
                    "minecraft:instant_health",
                    "minecraft:instant_damage",
                    "minecraft:saturation");
    public static final ForgeConfigSpec.BooleanValue ETERNAL_POTION_POUCH_WHITELIST;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>>
            ETERNAL_POTION_POUCH_EFFECT_LIST;

    // 真菌之心
    public static final ForgeConfigSpec.DoubleValue FUNGAL_HEART_MAX_HEALTH;
    public static final ForgeConfigSpec.DoubleValue FUNGAL_HEART_HEAL_PER_SECOND;

    // 黄金蜜药
    public static final ForgeConfigSpec.DoubleValue GOLDEN_HONEY_MEDICINE_MAX_HEALTH_BONUS;

    // 超幸运的四叶草！
    public static final ForgeConfigSpec.IntValue CLOVER_LUCK;
    public static final ForgeConfigSpec.IntValue CLOVER_FORTUNE;
    public static final ForgeConfigSpec.IntValue CLOVER_LOOTING;
    public static final ForgeConfigSpec.DoubleValue CLOVER_EMERALD_CHANCE;
    public static final ForgeConfigSpec.DoubleValue CLOVER_DIAMOND_CHANCE;
    public static final ForgeConfigSpec.DoubleValue CLOVER_DROP_CHANCE;

    // 暴躁河豚
    public static final ForgeConfigSpec.DoubleValue GRUMPY_PUFFERFISH_ATTACK_DAMAGE;

    // 菌盖伞
    public static final ForgeConfigSpec.DoubleValue FUNGUS_CAP_UMBRELLA_MAX_FALL_SPEED;
    public static final ForgeConfigSpec.DoubleValue FUNGUS_CAP_UMBRELLA_FALL_ACCELERATION;

    static {
        // ── mob.toml builder ──
        ForgeConfigSpec.Builder mob = new ForgeConfigSpec.Builder();

        mob.push("warped_fungus_sprite");
        WARPED_FUNGUS_SPRITE_ATTACK_DAMAGE = mob
                .comment("Attack damage of Warped Fungus Sprite (each 1.0 = half heart). Default: 6.0")
                .defineInRange("attackDamage", 6.0, 0.0, 1000.0);
        WARPED_FUNGUS_SPRITE_MAX_HEALTH = mob
                .comment("Max health of Warped Fungus Sprite (each 1.0 = half heart). Default: 24.0")
                .defineInRange("maxHealth", 24.0, 5.0, 10000.0);
        WARPED_FUNGUS_SPRITE_ARMOR = mob
                .comment("Armor of Warped Fungus Sprite. Default: 2.0")
                .defineInRange("armor", 2.0, 0.0, 100.0);
        mob.pop();

        mob.push("warped_fungus_umbrella");
        WARPED_FUNGUS_UMBRELLA_COLLISION_DAMAGE = mob
                .comment("Contact/collision damage of Warped Fungus Umbrella (each 1.0 = half heart). Default: 3.0")
                .defineInRange("collisionDamage", 3.0, 0.0, 1000.0);
        WARPED_FUNGUS_UMBRELLA_SPORE_DAMAGE = mob
                .comment("Damage of Fungus Spore projectile shot by Eerie Fungus Umbrella. Default: 4.0")
                .defineInRange("sporeDamage", 4.0, 0.0, 1000.0);
        WARPED_FUNGUS_UMBRELLA_MAX_HEALTH = mob
                .comment("Max health of Warped Fungus Umbrella (each 1.0 = half heart). Default: 30.0")
                .defineInRange("maxHealth", 30.0, 5.0, 10000.0);
        WARPED_FUNGUS_UMBRELLA_ARMOR = mob
                .comment("Armor of Warped Fungus Umbrella. Default: 2.0")
                .defineInRange("armor", 2.0, 0.0, 100.0);
        mob.pop();

        MOB_CONFIG = mob.build();

        // ── item.toml builder ──
        ForgeConfigSpec.Builder item = new ForgeConfigSpec.Builder();

        item.push("fungal_heart");
        FUNGAL_HEART_MAX_HEALTH = item
                .comment("Amount of max health added when wearing the Fungal Heart.",
                        "Each 1.0 = half a heart. Default: 2.0 (one full heart).")
                .defineInRange("fungalHeartMaxHealth", 2.0, 0.0, 1000.0);
        FUNGAL_HEART_HEAL_PER_SECOND = item
                .comment("Health regenerated per second while wearing the Fungal Heart.",
                        "Each 1.0 = half a heart. Default: 1.0.")
                .defineInRange("fungalHeartHealPerSecond", 1.0, 0.0, 100.0);
        item.pop();

        item.push("golden_honey_medicine");
        GOLDEN_HONEY_MEDICINE_MAX_HEALTH_BONUS = item
                .comment("Max health added while wearing the Golden Honey Medicine.",
                        "Each 1.0 = half a heart. Default: 5.0.")
                .defineInRange("goldenHoneyMedicineMaxHealthBonus", 5.0, 0.0, 1024.0);
        item.pop();

        item.push("lucky_clover");
        CLOVER_LUCK = item
                .comment("Luck attribute granted while wearing the Lucky Clover. Default: 1")
                .defineInRange("luckyCloverLuck", 1, 0, 10);
        CLOVER_FORTUNE = item
                .comment("Extra Fortune level applied while mining when wearing the Lucky Clover. Default: 1")
                .defineInRange("luckyCloverFortuneLevel", 1, 0, 3);
        CLOVER_LOOTING = item
                .comment("Extra Looting level applied to mob drops when wearing the Lucky Clover. Default: 1")
                .defineInRange("luckyCloverLootingLevel", 1, 0, 3);
        CLOVER_EMERALD_CHANCE = item
                .comment("Chance to drop an extra emerald when breaking a forge:stone block while wearing the Lucky Clover. Default: 0.01 (1%)")
                .defineInRange("luckyCloverEmeraldChance", 0.01, 0.0, 1.0);
        CLOVER_DIAMOND_CHANCE = item
                .comment("Chance to drop an extra diamond when breaking a forge:stone block while wearing the Lucky Clover. Default: 0.005 (0.5%)")
                .defineInRange("luckyCloverDiamondChance", 0.005, 0.0, 1.0);
        CLOVER_DROP_CHANCE = item
                .comment("Chance to drop the Lucky Clover when breaking grass / tall grass / fern / large fern. Default: 0.001 (0.1%)")
                .defineInRange("luckyCloverDropChance", 0.001, 0.0, 1.0);
        item.pop();

        item.push("grumpy_pufferfish");
        GRUMPY_PUFFERFISH_ATTACK_DAMAGE = item
                .comment("Attack damage added while wearing the Grumpy Pufferfish (each 1.0 = half heart). Default: 2.0")
                .defineInRange("grumpyPufferfishAttackDamage", 2.0, 0.0, 1000.0);
        item.pop();

        item.push("fungus_cap_umbrella");
        FUNGUS_CAP_UMBRELLA_MAX_FALL_SPEED = item
                .comment("Max downward fall speed (blocks/tick) while holding the Fungus Cap Umbrella in the MAIN hand.",
                        "Vanilla player terminal fall speed is about 3.92. Lower = slower descent. Default: 0.5")
                .defineInRange("fungusCapUmbrellaMaxFallSpeed", 0.5, 0.05, 10.0);
        FUNGUS_CAP_UMBRELLA_FALL_ACCELERATION = item
                .comment("Net downward acceleration (blocks/tick^2) while holding the umbrella in the MAIN hand and falling.",
                        "Vanilla player fall gravity is about 0.08. Lower = gentler acceleration. Default: 0.02")
                .defineInRange("fungusCapUmbrellaFallAcceleration", 0.02, 0.0, 0.08);
        item.pop();

        item.push("eternalPotionPouch");
        ETERNAL_POTION_POUCH_WHITELIST = item
                .comment("When false, effectList is a blacklist. When true, every effect in a potion must be listed.",
                        "The list contains MobEffect registry IDs, not potion item or Potion IDs.")
                .define("useWhitelist", DEFAULT_ETERNAL_POTION_POUCH_WHITELIST);
        ETERNAL_POTION_POUCH_EFFECT_LIST = item
                .comment("MobEffect registry IDs checked when storing a potion.",
                        "Blacklist: any listed effect rejects the whole potion.",
                        "Whitelist: every effect must be listed or the whole potion is rejected.",
                        "Invalid IDs are ignored with a warning.",
                        "If instant effects are allowed, they trigger once every 5 seconds.")
                .defineListAllowEmpty(
                        "effectList",
                        DEFAULT_ETERNAL_POTION_POUCH_EFFECT_LIST,
                        entry -> entry instanceof String);
        item.pop();
        ITEM_CONFIG = item.build();
    }

    public static void register(FMLJavaModLoadingContext loadingContext) {
        IEventBus modBus = loadingContext.getModEventBus();
        modBus.addListener(ModConfig::onConfigLoading);
        modBus.addListener(ModConfig::onConfigReloading);
        loadingContext.registerConfig(Type.COMMON, MOB_CONFIG, "eternal_trinkets/mob.toml");
        loadingContext.registerConfig(Type.COMMON, ITEM_CONFIG, "eternal_trinkets/item.toml");
    }

    private static void onConfigLoading(ModConfigEvent.Loading event) {
        logEternalPotionPouchConfig(event, "Loaded");
    }

    private static void onConfigReloading(ModConfigEvent.Reloading event) {
        logEternalPotionPouchConfig(event, "Reloaded");
    }

    private static void logEternalPotionPouchConfig(
            ModConfigEvent event, String action) {
        if (event.getConfig().getSpec() != ITEM_CONFIG) {
            return;
        }

        boolean whitelist = ETERNAL_POTION_POUCH_WHITELIST.get();
        Set<ResourceLocation> entries =
                EternalPotionPouchData.parseConfiguredEffectIds(
                        ETERNAL_POTION_POUCH_EFFECT_LIST.get());
        EternalTrinkets.LOGGER.debug(
                "{} eternal potion pouch config from item.toml: mode={} entries={}",
                action,
                whitelist ? "WHITELIST" : "BLACKLIST",
                entries);
    }

    private ModConfig() {
        throw new UnsupportedOperationException("utility class");
    }
}
