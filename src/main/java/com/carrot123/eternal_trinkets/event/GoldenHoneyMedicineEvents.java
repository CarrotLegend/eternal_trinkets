package com.carrot123.eternal_trinkets.event;

import com.carrot123.eternal_trinkets.EternalTrinkets;
import com.carrot123.eternal_trinkets.item.curio.ModCurioItems;
import com.carrot123.eternal_trinkets.item.curio.health.GoldenHoneyMedicineItem;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(modid = EternalTrinkets.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class GoldenHoneyMedicineEvents {

    private GoldenHoneyMedicineEvents() {
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }

        if (event.getEntity() instanceof Player deadPlayer) {
            findEquippedMedicine(deadPlayer)
                    .ifPresent(GoldenHoneyMedicineItem::resetTemporaryState);
        }

        if (!(event.getSource().getEntity() instanceof ServerPlayer killer)) {
            return;
        }

        findEquippedMedicine(killer).ifPresent(stack -> {
            if (killer.getRandom().nextDouble() < GoldenHoneyMedicineItem.EMPOWER_CHANCE) {
                GoldenHoneyMedicineItem.empower(stack, killer.level().getGameTime());
            }
        });
    }

    private static java.util.Optional<ItemStack> findEquippedMedicine(Player player) {
        return CuriosApi.getCuriosInventory(player).resolve()
                .flatMap(handler -> handler.findFirstCurio(ModCurioItems.GOLDEN_HONEY_MEDICINE.get()))
                .filter(result -> GoldenHoneyMedicineItem.BELT_SLOT.equals(
                        result.slotContext().identifier()))
                .map(result -> result.stack());
    }
}
