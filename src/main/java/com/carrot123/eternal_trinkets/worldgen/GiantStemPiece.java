package com.carrot123.eternal_trinkets.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

/**
 * 巨大诡异菌的结构片段，照抄原版 NetherFossilPieces.NetherFossilPiece。
 * 使用 BlockIgnoreProcessor.STRUCTURE_AND_AIR：忽略结构方块与模板中的空气，
 * 避免用空气铲平周围地形；箱子的 LootTable NBT 由模板放置原样保留。
 */
public class GiantStemPiece extends TemplateStructurePiece {

    public GiantStemPiece(StructureTemplateManager manager, ResourceLocation location,
            BlockPos pos, Rotation rotation) {
        super(ModStructures.GIANT_STEM_PIECE.get(), 0, manager, location, location.toString(),
                makeSettings(rotation), pos);
    }

    public GiantStemPiece(StructureTemplateManager manager, CompoundTag tag) {
        super(ModStructures.GIANT_STEM_PIECE.get(), tag, manager,
                location -> makeSettings(Rotation.valueOf(tag.getString("Rot"))));
    }

    private static StructurePlaceSettings makeSettings(Rotation rotation) {
        return new StructurePlaceSettings()
                .setRotation(rotation)
                .setMirror(Mirror.NONE)
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
    }

    public static void addPieces(StructureTemplateManager manager, StructurePieceAccessor pieces,
            RandomSource random, BlockPos pos) {
        Rotation rotation = Rotation.getRandom(random);
        pieces.addPiece(new GiantStemPiece(manager, GiantStemStructure.TEMPLATE, pos, rotation));
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        super.addAdditionalSaveData(context, tag);
        tag.putString("Rot", this.placeSettings.getRotation().name());
    }

    @Override
    protected void handleDataMarker(String name, BlockPos pos, ServerLevelAccessor level,
            RandomSource random, BoundingBox box) {
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager manager, ChunkGenerator generator,
            RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pos) {
        box.encapsulate(this.template.getBoundingBox(this.placeSettings, this.templatePosition));
        super.postProcess(level, manager, generator, random, box, chunkPos, pos);
    }
}
