package io.github.haykam821.snowballfight.game.map.fortress;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class FortressConfig {
	public static final Codec<FortressConfig> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			Codec.INT.optionalFieldOf("height", 7).forGetter(FortressConfig::getHeight)
		).apply(instance, FortressConfig::new);
	});
	
	private final int height;

	public FortressConfig(int height) {
		this.height = height;
	}

	public int getHeight() {
		return this.height;
	}
}