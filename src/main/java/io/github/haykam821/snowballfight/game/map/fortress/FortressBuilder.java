package io.github.haykam821.snowballfight.game.map.fortress;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import xyz.nucleoid.map_templates.MapTemplate;

public class FortressBuilder {
	private static final BlockState FORTRESS_STATE = Blocks.STONE_BRICKS.getDefaultState();
	private static final BlockState CRACKED_FORTRESS_STATE = Blocks.CRACKED_STONE_BRICKS.getDefaultState();
	private static final BlockState BANNER_STATE = Blocks.LIGHT_BLUE_WALL_BANNER.getDefaultState();

	private final FortressConfig config;

	public FortressBuilder(FortressConfig config) {
		this.config = config;
	}

	private BlockState getBannerFacing(Direction direction) {
		return BANNER_STATE.with(Properties.HORIZONTAL_FACING, direction);
	}

	public void build(MapTemplate template, BlockPos initialPos) {
		BlockPos.Mutable pos = initialPos.mutableCopy();
		Random random = Random.createLocal();

		for (int y = 0; y < this.config.getHeight(); y++) {
			for (int x = 0; x < 4; x++) {
				for (int z = 0; z < 4; z++) {
					pos.set(initialPos.getX() + x, initialPos.getY() + y, initialPos.getZ() + z);
					template.setBlockState(pos, random.nextInt(20) == 0 ? CRACKED_FORTRESS_STATE : FORTRESS_STATE);
				}
			}
		}

		// Banners
		int bannerY = this.config.getHeight() - 1;

		template.setBlockState(initialPos.add(0, bannerY, -1), this.getBannerFacing(Direction.NORTH));
		template.setBlockState(initialPos.add(3, bannerY, -1), this.getBannerFacing(Direction.NORTH));

		template.setBlockState(initialPos.add(4, bannerY, 0), this.getBannerFacing(Direction.EAST));
		template.setBlockState(initialPos.add(4, bannerY, 3), this.getBannerFacing(Direction.EAST));

		template.setBlockState(initialPos.add(0, bannerY, 4), this.getBannerFacing(Direction.SOUTH));
		template.setBlockState(initialPos.add(3, bannerY, 4), this.getBannerFacing(Direction.SOUTH));

		template.setBlockState(initialPos.add(-1, bannerY, 0), this.getBannerFacing(Direction.WEST));
		template.setBlockState(initialPos.add(-1, bannerY, 3), this.getBannerFacing(Direction.WEST));
	}
}