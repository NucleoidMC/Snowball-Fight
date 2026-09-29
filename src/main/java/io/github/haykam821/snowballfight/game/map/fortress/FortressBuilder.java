package io.github.haykam821.snowballfight.game.map.fortress;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import xyz.nucleoid.map_templates.MapTemplate;

public class FortressBuilder {
	private static final BlockState FORTRESS_STATE = Blocks.STONE_BRICKS.defaultBlockState();
	private static final BlockState CRACKED_FORTRESS_STATE = Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
	private static final BlockState BANNER_STATE = Blocks.WALL_BANNER.lightBlue().defaultBlockState();

	private final FortressConfig config;
	private final RandomSource random;

	public FortressBuilder(FortressConfig config, RandomSource random) {
		this.config = config;
		this.random = random;
	}

	private BlockState getBannerFacing(Direction direction) {
		return BANNER_STATE.setValue(BlockStateProperties.HORIZONTAL_FACING, direction);
	}

	public void build(MapTemplate template, BlockPos initialPos) {
		BlockPos.MutableBlockPos pos = initialPos.mutable();

		for (int y = 0; y < this.config.getHeight(); y++) {
			for (int x = 0; x < 4; x++) {
				for (int z = 0; z < 4; z++) {
					pos.set(initialPos.getX() + x, initialPos.getY() + y, initialPos.getZ() + z);
					template.setBlockState(pos, this.random.nextInt(20) == 0 ? CRACKED_FORTRESS_STATE : FORTRESS_STATE);
				}
			}
		}

		// Banners
		int bannerY = this.config.getHeight() - 1;

		template.setBlockState(initialPos.offset(0, bannerY, -1), this.getBannerFacing(Direction.NORTH));
		template.setBlockState(initialPos.offset(3, bannerY, -1), this.getBannerFacing(Direction.NORTH));

		template.setBlockState(initialPos.offset(4, bannerY, 0), this.getBannerFacing(Direction.EAST));
		template.setBlockState(initialPos.offset(4, bannerY, 3), this.getBannerFacing(Direction.EAST));

		template.setBlockState(initialPos.offset(0, bannerY, 4), this.getBannerFacing(Direction.SOUTH));
		template.setBlockState(initialPos.offset(3, bannerY, 4), this.getBannerFacing(Direction.SOUTH));

		template.setBlockState(initialPos.offset(-1, bannerY, 0), this.getBannerFacing(Direction.WEST));
		template.setBlockState(initialPos.offset(-1, bannerY, 3), this.getBannerFacing(Direction.WEST));
	}
}