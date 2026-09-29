package io.github.haykam821.snowballfight.game.map;

import io.github.haykam821.snowballfight.game.SnowballFightConfig;
import io.github.haykam821.snowballfight.game.map.fortress.FortressBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;

public class SnowballFightMapBuilder {
	private static final BlockState FLOOR = Blocks.BLUE_ICE.defaultBlockState();
	private static final BlockState WALL = Blocks.SPRUCE_WOOD.defaultBlockState();
	private static final BlockState WALL_UPPER = Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(BlockStateProperties.PERSISTENT, true);
	private static final BlockState FILL = Blocks.SNOW_BLOCK.defaultBlockState();

	private final SnowballFightConfig config;

	private final SimplexNoise noiseSampler;
	private final FortressBuilder fortressBuilder;

	public SnowballFightMapBuilder(SnowballFightConfig config, RandomSource random) {
		this.config = config;

		this.noiseSampler = new SimplexNoise(random);
		this.fortressBuilder = new FortressBuilder(this.config.getMapConfig().getFortressConfig(), random);
	}

	public SnowballFightMap create() {
		MapTemplate template = MapTemplate.createEmpty();
		SnowballFightMapConfig mapConfig = this.config.getMapConfig();

		int height = Math.max(mapConfig.getWallHeight(), mapConfig.getFortressConfig().getHeight() + 2);
		BlockBounds bounds = BlockBounds.of(BlockPos.ZERO, new BlockPos(mapConfig.getX() + 1, height, mapConfig.getZ() + 1));
		this.build(bounds, template, mapConfig);

		this.fortressBuilder.build(template, BlockPos.containing(bounds.center().x - 2, 1, bounds.center().z - 2));

		return new SnowballFightMap(template, bounds, mapConfig.getFortressConfig().getHeight());
	}

	private BlockState getBlockState(BlockPos pos, BlockBounds bounds, SnowballFightMapConfig mapConfig) {
		int layer = pos.getY() - bounds.min().getY();
		if (layer == 0) {
			return FLOOR;
		}

		boolean outline = pos.getX() == bounds.min().getX() || pos.getX() == bounds.max().getX() || pos.getZ() == bounds.min().getZ() || pos.getZ() == bounds.max().getZ();
		if (outline && layer <= mapConfig.getWallHeight()) {
			return layer < mapConfig.getWallHeight() - 2 ? WALL : WALL_UPPER;
		}

		double noiseScale = mapConfig.getNoiseScale();
		double sample = (this.noiseSampler.get(pos.getX() / noiseScale, pos.getZ() / noiseScale) + 1) / 2;

		if (layer <= sample * mapConfig.getSnowHeight() + 1) {
			return FILL;
		}

		return null;
	}

	public void build(BlockBounds bounds, MapTemplate template, SnowballFightMapConfig mapConfig) {
		for (BlockPos pos : bounds) {
			BlockState state = this.getBlockState(pos, bounds, mapConfig);
			if (state != null) {
				template.setBlockState(pos, state);
			}
		}
	}
}