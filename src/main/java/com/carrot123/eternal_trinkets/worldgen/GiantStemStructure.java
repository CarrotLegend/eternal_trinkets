package com.carrot123.eternal_trinkets.worldgen;

import java.util.Optional;
import java.util.function.Consumer;

import com.carrot123.eternal_trinkets.EternalTrinkets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

/**
 * 巨大诡异菌结构。
 * 完全照抄原版 NetherFossilStructure 的下界放置模式：从一个采样高度向下扫描底部噪声列，
 * 找到"空气格 + 下方为可站立方块"的地板位置，再在该处放置结构模板。
 * 这是注册进结构注册表的真正结构，因此 /locate structure eternal_trinkets:giant_stem 可用。
 */
public class GiantStemStructure extends Structure {
    public static final Codec<GiantStemStructure> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    settingsCodec(instance),
                    HeightProvider.CODEC.fieldOf("height").forGetter(s -> s.height)
            ).apply(instance, GiantStemStructure::new));

    public static final ResourceLocation TEMPLATE =
            ResourceLocation.fromNamespaceAndPath(EternalTrinkets.MODID, "giant_stem");

    public final HeightProvider height;

    public GiantStemStructure(Structure.StructureSettings settings, HeightProvider height) {
        super(settings);
        this.height = height;
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        WorldgenRandom random = context.random();
        int x = context.chunkPos().getMinBlockX() + random.nextInt(16);
        int z = context.chunkPos().getMinBlockZ() + random.nextInt(16);
        int seaLevel = context.chunkGenerator().getSeaLevel();
        WorldGenerationContext genContext =
                new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor());
        int y = this.height.sample(random, genContext);
        NoiseColumn column = context.chunkGenerator()
                .getBaseColumn(x, z, context.heightAccessor(), context.randomState());
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(x, y, z);

        while (y > seaLevel) {
            BlockState state = column.getBlock(y);
            BlockState below = column.getBlock(--y);
            if (state.isAir()
                    && below.isFaceSturdy(EmptyBlockGetter.INSTANCE, mutable.setY(y), Direction.UP)) {
                break;
            }
        }

        if (y <= seaLevel) {
            return Optional.empty();
        }

        BlockPos pos = new BlockPos(x, y, z);
        return Optional.of(new Structure.GenerationStub(pos,
                (Consumer<StructurePiecesBuilder>) builder ->
                        GiantStemPiece.addPieces(context.structureTemplateManager(), builder, random, pos)));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.GIANT_STEM.get();
    }
}
