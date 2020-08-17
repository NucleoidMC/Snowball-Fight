package io.github.haykam821.snowballfight.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.haykam821.snowballfight.game.map.SnowballFightMapConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import xyz.nucleoid.plasmid.game.config.PlayerConfig;

public class SnowballFightConfig {
	public static final Codec<SnowballFightConfig> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			SnowballFightMapConfig.CODEC.fieldOf("map").forGetter(SnowballFightConfig::getMapConfig),
			PlayerConfig.CODEC.fieldOf("players").forGetter(SnowballFightConfig::getPlayerConfig),
			ItemStack.CODEC.optionalFieldOf("snowball_stack", new ItemStack(Items.SNOWBALL)).forGetter(SnowballFightConfig::getSnowballStack)
		).apply(instance, SnowballFightConfig::new);
	});

	private final SnowballFightMapConfig mapConfig;
	private final PlayerConfig playerConfig;
	private final ItemStack snowballStack;

	public SnowballFightConfig(SnowballFightMapConfig mapConfig, PlayerConfig playerConfig, ItemStack snowballStack) {
		this.mapConfig = mapConfig;
		this.playerConfig = playerConfig;
		this.snowballStack = snowballStack;
	}

	public SnowballFightMapConfig getMapConfig() {
		return this.mapConfig;
	}

	public PlayerConfig getPlayerConfig() {
		return this.playerConfig;
	}

	public ItemStack getSnowballStack() {
		return this.snowballStack;
	}
}