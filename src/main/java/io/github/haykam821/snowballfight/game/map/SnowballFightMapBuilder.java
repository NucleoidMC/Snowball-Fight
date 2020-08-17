package io.github.haykam821.snowballfight.game.map;

import java.util.concurrent.CompletableFuture;

import io.github.haykam821.snowballfight.game.SnowballFightConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.plasmid.game.map.template.MapTemplate;
import xyz.nucleoid.plasmid.util.BlockBounds;

public class SnowballFightMapBuilder {
	private static final BlockState FLOOR = Blocks.BLUE_ICE.getDefaultState();
	private static final BlockState WALL = Blocks.SPRUCE_WOOD.getDefaultState();
	private static final BlockState WALL_UPPER = Blocks.SPRUCE_LEAVES.getDefaultState().with(Properties.PERSISTENT, true);
	private static final BlockState FILL = Blocks.SNOW_BLOCK.getDefaultState();

	private final SnowballFightConfig config;

	public SnowballFightMapBuilder(SnowballFightConfig config) {
		this.config = config;
	}

	public CompletableFuture<SnowballFightMap> create() {
		return CompletableFuture.supplyAsync(() -> {
			MapTemplate template = MapTemplate.createEmpty();
			SnowballFightMapConfig mapConfig = this.config.getMapConfig();

			BlockBounds bounds = new BlockBounds(BlockPos.ORIGIN, new BlockPos(mapConfig.getX() + 1, mapConfig.getWallHeight(), mapConfig.getZ() + 1));
			this.build(bounds, template, mapConfig);

			return new SnowballFightMap(template, bounds);
		}, Util.getMainWorkerExecutor());
	}

	private BlockState getBlockState(BlockPos pos, BlockBounds bounds, SnowballFightMapConfig mapConfig) {
		int layer = pos.getY() - bounds.getMin().getY();
		if (layer == 0) {
			return FLOOR;
		}

		boolean outline = pos.getX() == bounds.getMin().getX() || pos.getX() == bounds.getMax().getX() || pos.getZ() == bounds.getMin().getZ() || pos.getZ() == bounds.getMax().getZ();
		if (outline) {
			return layer > mapConfig.getSnowHeight() + 2 ? WALL_UPPER : WALL;
		} else if (layer <= mapConfig.getSnowHeight()) {
			return FILL;
		}

		return null;
	}

	public void build(BlockBounds bounds, MapTemplate template, SnowballFightMapConfig mapConfig) {
		for (BlockPos pos : bounds.iterate()) {
			BlockState state = this.getBlockState(pos, bounds, mapConfig);
			if (state != null) {
				template.setBlockState(pos, state);
			}
		}
	}
}