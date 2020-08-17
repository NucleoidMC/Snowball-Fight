package io.github.haykam821.snowballfight;

import io.github.haykam821.snowballfight.game.SnowballFightConfig;
import io.github.haykam821.snowballfight.game.phase.SnowballFightWaitingPhase;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.GameType;

public class Main implements ModInitializer {
	public static final String MOD_ID = "snowballfight";

	private static final Identifier SNOWBALL_PROVIDERS_ID = new Identifier(MOD_ID, "snowball_providers");
	public static final Tag<Block> SNOWBALL_PROVIDERS = TagRegistry.block(SNOWBALL_PROVIDERS_ID);

	private static final Identifier SNOWBALL_FIGHT_ID = new Identifier(MOD_ID, "snowball_fight");
	public static final GameType<SnowballFightConfig> SNOWBALL_FIGHT_TYPE = GameType.register(SNOWBALL_FIGHT_ID, SnowballFightWaitingPhase::open, SnowballFightConfig.CODEC);

	@Override
	public void onInitialize() {
		return;
	}
}