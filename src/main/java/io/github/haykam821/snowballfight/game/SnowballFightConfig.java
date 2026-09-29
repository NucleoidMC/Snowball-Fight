package io.github.haykam821.snowballfight.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.haykam821.snowballfight.game.map.SnowballFightMapConfig;
import net.minecraft.SharedConstants;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import xyz.nucleoid.plasmid.api.game.common.config.WaitingLobbyConfig;

public class SnowballFightConfig {
	public static final MapCodec<SnowballFightConfig> CODEC = RecordCodecBuilder.mapCodec(instance -> {
		return instance.group(
			SnowballFightMapConfig.CODEC.fieldOf("map").forGetter(SnowballFightConfig::getMapConfig),
			WaitingLobbyConfig.CODEC.fieldOf("players").forGetter(SnowballFightConfig::getPlayerConfig),
			IntProviders.NON_NEGATIVE_CODEC.optionalFieldOf("ticks_until_close", ConstantInt.of(SharedConstants.TICKS_PER_SECOND * 5)).forGetter(SnowballFightConfig::getTicksUntilClose),
			ItemStackTemplate.CODEC.optionalFieldOf("snowball_stack", new ItemStackTemplate(Items.SNOWBALL)).forGetter(SnowballFightConfig::getSnowballStack),
			Codec.BOOL.optionalFieldOf("allow_self_eliminating", false).forGetter(SnowballFightConfig::shouldAllowSelfEliminating)
		).apply(instance, SnowballFightConfig::new);
	});

	private final SnowballFightMapConfig mapConfig;
	private final WaitingLobbyConfig playerConfig;
	private final IntProvider ticksUntilClose;
	private final ItemStackTemplate snowballStack;
	private final boolean allowSelfEliminating;

	public SnowballFightConfig(SnowballFightMapConfig mapConfig, WaitingLobbyConfig playerConfig, IntProvider ticksUntilClose, ItemStackTemplate snowballStack, boolean allowSelfEliminating) {
		this.mapConfig = mapConfig;
		this.playerConfig = playerConfig;
		this.ticksUntilClose = ticksUntilClose;
		this.snowballStack = snowballStack;
		this.allowSelfEliminating = allowSelfEliminating;
	}

	public SnowballFightMapConfig getMapConfig() {
		return this.mapConfig;
	}

	public WaitingLobbyConfig getPlayerConfig() {
		return this.playerConfig;
	}

	public IntProvider getTicksUntilClose() {
		return this.ticksUntilClose;
	}

	public ItemStackTemplate getSnowballStack() {
		return this.snowballStack;
	}

	public boolean shouldAllowSelfEliminating() {
		return this.allowSelfEliminating;
	}
}