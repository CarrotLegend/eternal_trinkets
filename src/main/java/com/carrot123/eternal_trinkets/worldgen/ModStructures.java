package com.carrot123.eternal_trinkets.worldgen;

import com.carrot123.eternal_trinkets.EternalTrinkets;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("null")
public final class ModStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, EternalTrinkets.MODID);

    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, EternalTrinkets.MODID);

    public static final RegistryObject<StructureType<GiantStemStructure>> GIANT_STEM =
            STRUCTURE_TYPES.register("giant_stem",
                    () -> (StructureType<GiantStemStructure>) () -> GiantStemStructure.CODEC);

    public static final RegistryObject<StructurePieceType> GIANT_STEM_PIECE =
            STRUCTURE_PIECES.register("giant_stem",
                    () -> (StructurePieceType.StructureTemplateType) GiantStemPiece::new);

    private ModStructures() {
        throw new UnsupportedOperationException("utility class");
    }

    public static void register(IEventBus bus) {
        STRUCTURE_TYPES.register(bus);
        STRUCTURE_PIECES.register(bus);
    }
}
