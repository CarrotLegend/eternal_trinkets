package com.carrot123.eternal_trinkets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrot123.eternal_trinkets.block.ModBlocks;
import com.carrot123.eternal_trinkets.client.VoidCaptureDeviceClient;
import com.carrot123.eternal_trinkets.client.curio.GrumpyPufferfishCurioModel;
import com.carrot123.eternal_trinkets.client.curio.GrumpyPufferfishCurioRenderer;
import com.carrot123.eternal_trinkets.client.curio.LuckyCloverCurioModel;
import com.carrot123.eternal_trinkets.client.curio.LuckyCloverCurioRenderer;
import com.carrot123.eternal_trinkets.entity.ModEntities;
import com.carrot123.eternal_trinkets.entity.client.FungusSporeBulletRenderer;
import com.carrot123.eternal_trinkets.entity.client.WarpedFungusSpriteRenderer;
import com.carrot123.eternal_trinkets.entity.client.WarpedFungusUmbrellaRenderer;
import com.carrot123.eternal_trinkets.entity.neutral.WarpedFungusSprite;
import com.carrot123.eternal_trinkets.entity.neutral.WarpedFungusUmbrella;
import com.carrot123.eternal_trinkets.event.FungusCapUmbrellaEvents;
import com.carrot123.eternal_trinkets.event.ForestCrownEvents;
import com.carrot123.eternal_trinkets.event.ExtinctionStoneAttributeEvents;
import com.carrot123.eternal_trinkets.event.HellEyeCombatEvents;
import com.carrot123.eternal_trinkets.event.AngelBlazeGuardEvents;
import com.carrot123.eternal_trinkets.event.HatredDiaryEvents;
import com.carrot123.eternal_trinkets.event.ArcherAimingScopeEvents;
import com.carrot123.eternal_trinkets.event.GrumpyPufferfishEvents;
import com.carrot123.eternal_trinkets.event.LuckyCloverEvents;
import com.carrot123.eternal_trinkets.event.NewCurioEvents;
import com.carrot123.eternal_trinkets.event.StarlightStingEvents;
import com.carrot123.eternal_trinkets.event.WarpedCoreEvents;
import com.carrot123.eternal_trinkets.event.WarpedFungusCapBoatEvents;
import com.carrot123.eternal_trinkets.event.YinYangCombatEvents;
import com.carrot123.eternal_trinkets.integration.thirst.ModThirstItems;
import com.carrot123.eternal_trinkets.item.ModCreativeTabs;
import com.carrot123.eternal_trinkets.item.ModItems;
import com.carrot123.eternal_trinkets.item.curio.ModCurioItems;
import com.carrot123.eternal_trinkets.misc.ModConfig;
import com.carrot123.eternal_trinkets.misc.ModEffects;
import com.carrot123.eternal_trinkets.misc.ModSounds;
import com.carrot123.eternal_trinkets.network.ModNetwork;
import com.carrot123.eternal_trinkets.worldgen.ModStructures;

import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

@Mod(EternalTrinkets.MODID)
public final class EternalTrinkets {

    public static final String MODID = "eternal_trinkets";
    public static final Logger LOGGER =
            LoggerFactory.getLogger("Eternal Trinkets");

    public EternalTrinkets(FMLJavaModLoadingContext loadingContext) {
        IEventBus bus = loadingContext.getModEventBus();

        ModConfig.register(loadingContext);
        ModEffects.register(bus);
        ModSounds.register(bus);
        ModCurioItems.ITEMS.register(bus);
        ModBlocks.register(bus);
        ModItems.register(bus);
        ModEntities.register(bus);
        ModCreativeTabs.register(bus);
        ModStructures.register(bus);
        ModThirstItems.register(bus);
        ModNetwork.register();

        bus.addListener(this::onEntityAttributeCreation);
        bus.addListener(this::onRegisterSpawnPlacements);

        MinecraftForge.EVENT_BUS.register(new WarpedCoreEvents());
        MinecraftForge.EVENT_BUS.register(new LuckyCloverEvents());
        MinecraftForge.EVENT_BUS.register(new GrumpyPufferfishEvents());
        MinecraftForge.EVENT_BUS.register(new FungusCapUmbrellaEvents());
        MinecraftForge.EVENT_BUS.register(new WarpedFungusCapBoatEvents());
        MinecraftForge.EVENT_BUS.register(new YinYangCombatEvents());
        MinecraftForge.EVENT_BUS.register(new StarlightStingEvents());
        MinecraftForge.EVENT_BUS.register(new NewCurioEvents());
        MinecraftForge.EVENT_BUS.register(new ForestCrownEvents());
        MinecraftForge.EVENT_BUS.register(new ExtinctionStoneAttributeEvents());
        MinecraftForge.EVENT_BUS.register(new HellEyeCombatEvents());
        MinecraftForge.EVENT_BUS.register(new AngelBlazeGuardEvents());
        MinecraftForge.EVENT_BUS.register(new HatredDiaryEvents());
        MinecraftForge.EVENT_BUS.register(new ArcherAimingScopeEvents());
    }

    private void onEntityAttributeCreation(
            EntityAttributeCreationEvent event) {
        event.put(
                ModEntities.WARPED_FUNGUS_SPRITE.get(),
                WarpedFungusSprite.createAttributes().build());

        event.put(
                ModEntities.WARPED_FUNGUS_UMBRELLA.get(),
                WarpedFungusUmbrella.createAttributes().build());
    }

    private void onRegisterSpawnPlacements(
            SpawnPlacementRegisterEvent event) {
        event.register(
                ModEntities.WARPED_FUNGUS_SPRITE.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR);
    }

    @Mod.EventBusSubscriber(
            modid = MODID,
            bus = Mod.EventBusSubscriber.Bus.MOD,
            value = Dist.CLIENT)
    public static final class ClientModEvents {

        @SubscribeEvent
        public static void onRegisterRenderers(
                EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(
                    ModEntities.WARPED_FUNGUS_SPRITE.get(),
                    WarpedFungusSpriteRenderer::new);

            event.registerEntityRenderer(
                    ModEntities.WARPED_FUNGUS_UMBRELLA.get(),
                    WarpedFungusUmbrellaRenderer::new);

            event.registerEntityRenderer(
                    ModEntities.FUNGUS_SPORE_BULLET.get(),
                    FungusSporeBulletRenderer::new);

            event.registerEntityRenderer(
                    ModEntities.WARPED_FUNGUS_CAP_BOAT.get(),
                    com.carrot123.eternal_trinkets.entity.client.WarpedFungusCapBoatRenderer::new);
        }

        @SubscribeEvent
        public static void onRegisterLayers(
                EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(
                    LuckyCloverCurioModel.LAYER,
                    LuckyCloverCurioModel::createLayer);

            event.registerLayerDefinition(
                    GrumpyPufferfishCurioModel.LAYER,
                    GrumpyPufferfishCurioModel::createLayer);
        }

        @SubscribeEvent
        public static void onRegisterAdditionalModels(
                net.minecraftforge.client.event.ModelEvent.RegisterAdditional event) {
            event.register(
                    com.carrot123.eternal_trinkets.client.item.FungusCapUmbrellaRenderer.GUI_MODEL);
        }

        @SubscribeEvent
        public static void onClientSetup(
                FMLClientSetupEvent event) {
            event.enqueueWork(() ->
                    VoidCaptureDeviceClient.registerProperty(
                            ModItems.VOID_CAPTURE_DEVICE.get()));

            event.enqueueWork(() ->
                    CuriosRendererRegistry.register(
                            ModCurioItems.LUCKY_CLOVER.get(),
                            LuckyCloverCurioRenderer::new));

            event.enqueueWork(() ->
                    CuriosRendererRegistry.register(
                            ModCurioItems.GRUMPY_PUFFERFISH.get(),
                            GrumpyPufferfishCurioRenderer::new));
        }
    }
}
