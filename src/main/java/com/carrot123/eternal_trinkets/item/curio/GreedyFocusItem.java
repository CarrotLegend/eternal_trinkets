package com.carrot123.eternal_trinkets.item.curio;

import com.carrot123.eternal_trinkets.EternalTrinkets;
import com.carrot123.eternal_trinkets.network.ModNetwork;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.CuriosApi;

public final class GreedyFocusItem extends BaseCurioItem {
    public static final int MAX_ABSORBED = 6;
    public static final String TAG_ABSORBED = "AbsorbedEmptyFocus";
    private static final ResourceLocation EMPTY_FOCUS = new ResourceLocation("goety", "empty_focus");
    private static final ResourceLocation FOCUS_DAMAGE = new ResourceLocation("until_eternity", "focus_damage");
    private static final ResourceLocation COOLDOWN = new ResourceLocation("goety", "cooldown_discount");
    private static final UUID DAMAGE_UUID = stableUuid("focus_damage");
    private static final UUID COOLDOWN_UUID = stableUuid("cooldown_discount");

    public GreedyFocusItem() {
        super(Rarity.RARE);
    }

    private static UUID stableUuid(String name) {
        return UUID.nameUUIDFromBytes((EternalTrinkets.MODID + ":greedy_focus/" + name)
                .getBytes(StandardCharsets.UTF_8));
    }

    public static int absorbed(ItemStack stack) {
        return Math.max(0, Math.min(MAX_ABSORBED,
                stack.hasTag() ? stack.getTag().getInt(TAG_ABSORBED) : 0));
    }

    private static Attribute getAttribute(ResourceLocation id) {
        return ModList.get().isLoaded(id.getNamespace())
                ? ForgeRegistries.ATTRIBUTES.getValue(id) : null;
    }

    @Override
    public boolean canEquip(SlotContext context, ItemStack stack) {
        if (!"charm".equals(context.identifier())) {
            return false;
        }
        return CuriosApi.getCuriosInventory(context.entity()).map(handler -> {
            var charm = handler.getCurios().get("charm");
            if (charm == null) {
                return true;
            }
            var stacks = charm.getStacks();
            for (int i = 0; i < stacks.getSlots(); i++) {
                if (i != context.index() && stacks.getStackInSlot(i).is(this)) {
                    return false;
                }
            }
            return true;
        }).orElse(true);
    }

    @Override
    public boolean canEquipFromUse(SlotContext context, ItemStack stack) {
        return canEquip(context, stack);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext context, UUID slotUuid, ItemStack stack) {
        if (!"charm".equals(context.identifier())) {
            return ImmutableMultimap.of();
        }
        ImmutableMultimap.Builder<Attribute, AttributeModifier> result = ImmutableMultimap.builder();
        Attribute cooldown = getAttribute(COOLDOWN);
        if (cooldown != null) {
            result.put(cooldown, new AttributeModifier(COOLDOWN_UUID,
                    "greedy_focus_cooldown", 0.10D, AttributeModifier.Operation.ADDITION));
        }
        Attribute damage = getAttribute(FOCUS_DAMAGE);
        if (damage != null && absorbed(stack) > 0) {
            result.put(damage, new AttributeModifier(DAMAGE_UUID,
                    "greedy_focus_damage", absorbed(stack) * 0.05D,
                    AttributeModifier.Operation.MULTIPLY_BASE));
        }
        return result.build();
    }

    @Override
    public void curioTick(SlotContext context, ItemStack stack) {
        if (context.entity().level().isClientSide || !"charm".equals(context.identifier())) {
            return;
        }
        Attribute damage = getAttribute(FOCUS_DAMAGE);
        AttributeInstance instance = damage == null ? null : context.entity().getAttribute(damage);
        if (instance == null) {
            return;
        }
        double desired = absorbed(stack) * 0.05D;
        AttributeModifier current = instance.getModifier(DAMAGE_UUID);
        if (current != null && Math.abs(current.getAmount() - desired) < 0.00001D) {
            return;
        }
        if (current != null) {
            instance.removeModifier(DAMAGE_UUID);
        }
        if (desired > 0.0D) {
            instance.addTransientModifier(new AttributeModifier(DAMAGE_UUID,
                    "greedy_focus_damage", desired, AttributeModifier.Operation.MULTIPLY_BASE));
        }
    }

    @Override
    public void onUnequip(SlotContext context, ItemStack newStack, ItemStack stack) {
        if (newStack.is(this)) {
            return;
        }
        Attribute damage = getAttribute(FOCUS_DAMAGE);
        AttributeInstance instance = damage == null ? null : context.entity().getAttribute(damage);
        if (instance != null) {
            instance.removeModifier(DAMAGE_UUID);
        }
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack focusStack, ItemStack carried,
            Slot slot, ClickAction action, Player player, SlotAccess carriedAccess) {
        if (action != ClickAction.SECONDARY || !isEmptyFocus(carried)) {
            return false;
        }
        if (player.level().isClientSide) {
            if (player.getAbilities().instabuild) {
                int inventorySlot = slot.getContainerSlot();
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                        () -> () -> ModNetwork.requestCreativeFocusAbsorb(inventorySlot, carried.copy()));
            }
            return true;
        }
        if (absorbed(focusStack) >= MAX_ABSORBED) {
            return true;
        }
        focusStack.getOrCreateTag().putInt(TAG_ABSORBED, absorbed(focusStack) + 1);
        if (!player.getAbilities().instabuild) {
            carried.shrink(1);
        }
        slot.setChanged();
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.inventoryMenu.broadcastChanges();
        }
        return true;
    }

    public static boolean absorbCreative(ServerPlayer player, int inventorySlot) {
        if (!player.gameMode.isCreative() || inventorySlot < 0
                || inventorySlot >= player.getInventory().items.size()
                || !ModList.get().isLoaded("goety")) {
            return false;
        }
        ItemStack stack = player.getInventory().getItem(inventorySlot);
        if (!(stack.getItem() instanceof GreedyFocusItem) || absorbed(stack) >= MAX_ABSORBED) {
            return false;
        }
        stack.getOrCreateTag().putInt(TAG_ABSORBED, absorbed(stack) + 1);
        player.inventoryMenu.broadcastChanges();
        return true;
    }

    public static boolean isEmptyFocus(ItemStack stack) {
        Item item = ModList.get().isLoaded("goety") ? ForgeRegistries.ITEMS.getValue(EMPTY_FOCUS) : null;
        return item != null && stack.is(item);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
            List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (ModList.get().isLoaded("goety")) {
            tooltip.add(Component.translatable("tooltip.eternal_trinkets.greedy_focus.cooldown")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("tooltip.eternal_trinkets.optional_unavailable", "goety")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        tooltip.add(Component.translatable("tooltip.eternal_trinkets.greedy_focus.absorbed",
                absorbed(stack), MAX_ABSORBED).withStyle(ChatFormatting.GRAY));
        if (ModList.get().isLoaded("until_eternity")) {
            tooltip.add(Component.translatable("tooltip.eternal_trinkets.greedy_focus.damage",
                    absorbed(stack) * 5).withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("tooltip.eternal_trinkets.optional_unavailable",
                    "until_eternity").withStyle(ChatFormatting.DARK_GRAY));
        }
        tooltip.add(Component.translatable("tooltip.eternal_trinkets.greedy_focus.use")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
