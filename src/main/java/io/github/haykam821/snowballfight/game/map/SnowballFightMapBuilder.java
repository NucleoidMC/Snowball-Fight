package io.github.haykam821.snowballfight.game.map;

import io.github.haykam821.snowballfight.game.SnowballFightConfig;
import io.github.haykam821.snowballfight.game.map.fortress.FortressBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;

public class SnowballFightMapBuilder {
	private static final BlockState FLOOR = Blocks.BLUE_ICE.getDefaultState();
	private static final BlockState WALL = Blocks.SPRUCE_WOOD.getDefaultState();
	private static final BlockState WALL_UPPER = Blocks.SPRUCE_LEAVES.getDefaultState().with(Properties.PERSISTENT, true);
	private static final BlockState FILL = Blocks.SNOW_BLOCK.getDefaultState();

	private final SnowballFightConfig config;

	public SnowballFightMapBuilder(SnowballFightConfig config) {
		this.config = config;
	}

	public SnowballFightMap create() {
		MapTemplate template = MapTemplate.createEmpty();
		SnowballFightMapConfig mapConfig = this.config.getMapConfig();

		int height = Math.max(mapConfig.getWallHeight(), mapConfig.getFortressConfig().getHeight() + 2);
		BlockBounds bounds = BlockBounds.of(BlockPos.ORIGIN, new BlockPos(mapConfig.getX() + 1, height, mapConfig.getZ() + 1));
		this.build(bounds, template, mapConfig);

		FortressBuilder fortressBuilder = new FortressBuilder(mapConfig.getFortressConfig());
		fortressBuilder.build(template, BlockPos.ofFloored(bounds.center().x - 2, 1, bounds.center().z - 2));

		return new SnowballFightMap(template, bounds, mapConfig.getFortressConfig().getHeight());
	}

	private BlockState getBlockState(BlockPos pos, BlockBounds bounds, SnowballFightMapConfig mapConfig) {
		int layer = pos.getY() - bounds.min().getY();
		if (layer == 0) {
			return FLOOR;
		}

		boolean outline = pos.getX() == bounds.min().getX() || pos.getX() == bounds.max().getX() || pos.getZ() == bounds.min().getZ() || pos.getZ() == bounds.max().getZ();
		if (outline && layer <= mapConfig.getWallHeight()) {
			return layer > mapConfig.getSnowHeight() + 2 ? WALL_UPPER : WALL;
		} else if (layer <= mapConfig.getSnowHeight()) {
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