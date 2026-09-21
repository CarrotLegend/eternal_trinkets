package com.carrot123.eternal_trinkets.item;

import java.util.List;
import java.util.function.Predicate;

import com.carrot123.eternal_trinkets.entity.vehicle.WarpedFungusCapBoat;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class WarpedFungusCapBoatItem extends Item {

    private static final Predicate<Entity> ENTITY_PREDICATE = EntitySelector.NO_SPECTATORS.and(Entity::isPickable);

    public WarpedFungusCapBoatItem(Item.Properties properties) {
        super(properties);
    }

    // ── tooltip ──────────────────────────────────────────────────
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                 List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.eternal_trinkets.warped_fungus_cap_boat.lava"));
        tooltip.add(Component.translatable("tooltip.eternal_trinkets.warped_fungus_cap_boat.soulsand"));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        HitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        if (hit.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(stack);
        } else {
            Vec3 view = player.getViewVector(1.0F);
            double reach = 5.0;
            List<Entity> nearby = level.getEntities(player,
                    player.getBoundingBox().expandTowards(view.scale(reach)).inflate(1.0), ENTITY_PREDICATE);
            if (!nearby.isEmpty()) {
                Vec3 eye = player.getEyePosition();

                for (Entity entity : nearby) {
                    AABB box = entity.getBoundingBox().inflate(entity.getPickRadius());
                    if (box.contains(eye)) {
                        return InteractionResultHolder.pass(stack);
                    }
                }
            }

            if (hit.getType() == HitResult.Type.BLOCK) {
                WarpedFungusCapBoat boat = new WarpedFungusCapBoat(level,
                        hit.getLocation().x, hit.getLocation().y, hit.getLocation().z);
                boat.setYRot(player.getYRot());
                if (!level.noCollision(boat, boat.getBoundingBox())) {
                    return InteractionResultHolder.fail(stack);
                } else {
                    if (!level.isClientSide) {
                        level.addFreshEntity(boat);
                        level.gameEvent(player, GameEvent.ENTITY_PLACE, hit.getLocation());
                        if (!player.getAbilities().instabuild) {
                            stack.shrink(1);
                        }
                    }

                    player.awardStat(Stats.ITEM_USED.get(this));
                    return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
                }
            } else {
                return InteractionResultHolder.pass(stack);
            }
        }
    }
}
