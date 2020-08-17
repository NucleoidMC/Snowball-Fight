package io.github.haykam821.snowballfight.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.haykam821.snowballfight.game.map.fortress.FortressConfig;

public class SnowballFightMapConfig {
	public static final Codec<SnowballFightMapConfig> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			Codec.INT.fieldOf("x").forGetter(SnowballFightMapConfig::getX),
			Codec.INT.fieldOf("z").forGetter(SnowballFightMapConfig::getZ),
			Codec.INT.optionalFieldOf("wall_height", 8).forGetter(SnowballFightMapConfig::getWallHeight),
			Codec.INT.optionalFieldOf("snow_height", 3).forGetter(SnowballFightMapConfig::getSnowHeight),
			FortressConfig.CODEC.optionalFieldOf("fortress", new FortressConfig(7)).forGetter(SnowballFightMapConfig::getFortressConfig)
		).apply(instance, SnowballFightMapConfig::new);
	});

	private final int x;
	private final int z;
	private final int wallHeight;
	private final int snowHeight;
	private final FortressConfig fortressConfig;

	public SnowballFightMapConfig(int x, int z, int wallHeight, int snowHeight, FortressConfig fortressConfig) {
		this.x = x;
		this.z = z;
		this.wallHeight = wallHeight;
		this.snowHeight = snowHeight;
		this.fortressConfig = fortressConfig;
	}

	public int getX() {
		return this.x;
	}

	public int getZ() {
		return this.z;
	}

	public int getWallHeight() {
		return this.wallHeight;
	}

	public int getSnowHeight() {
		return this.snowHeight;
	}

	public FortressConfig getFortressConfig() {
		return this.fortressConfig;
	}
}