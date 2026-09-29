package com.carrot123.eternal_trinkets.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class GiantStemPiece extends TemplateStructurePiece {

    public GiantStemPiece(
            StructureTemplateManager manager,
            ResourceLocation location,
            BlockPos pos,
            Rotation rotation
    ) {
        super(
                ModStructures.GIANT_STEM_PIECE.get(),
                0,
                manager,
                location,
                location.toString(),
                makeSettings(rotation),
                pos
        );
    }

    public GiantStemPiece(
            StructureTemplateManager manager,
            CompoundTag tag
    ) {
        super(
                ModStructures.GIANT_STEM_PIECE.get(),
                tag,
                manager,
                location -> makeSettings(
                        Rotation.valueOf(
                                tag.getString("Rot")
                        )
                )
        );
    }

    private static StructurePlaceSettings makeSettings(
            Rotation rotation
    ) {
        return new StructurePlaceSettings()
                .setRotation(rotation)
                .setMirror(Mirror.NONE)
                .addProcessor(
                        BlockIgnoreProcessor.STRUCTURE_AND_AIR
                );
    }

    public static void addPieces(
            StructureTemplateManager manager,
            StructurePieceAccessor pieces,
            RandomSource random,
            BlockPos pos
    ) {
        Rotation rotation =
                Rotation.getRandom(random);

        pieces.addPiece(
                new GiantStemPiece(
                        manager,
                        GiantStemStructure.TEMPLATE,
                        pos,
                        rotation
                )
        );
    }

    @Override
    protected void addAdditionalSaveData(
            StructurePieceSerializationContext context,
            CompoundTag tag
    ) {
        super.addAdditionalSaveData(
                context,
                tag
        );

        tag.putString(
                "Rot",
                this.placeSettings
                        .getRotation()
                        .name()
        );
    }

    @Override
    protected void handleDataMarker(
            String name,
            BlockPos pos,
            ServerLevelAccessor level,
            RandomSource random,
            BoundingBox box
    ) {
    }
}