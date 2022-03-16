package io.github.haykam821.snowballfight;

import io.github.haykam821.snowballfight.game.SnowballFightConfig;
import io.github.haykam821.snowballfight.game.phase.SnowballFightWaitingPhase;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.Block;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import xyz.nucleoid.plasmid.game.GameType;

public class Main implements ModInitializer {
	public static final String MOD_ID = "snowballfight";

	private static final Identifier SNOWBALL_PROVIDERS_ID = new Identifier(MOD_ID, "snowball_providers");
	public static final TagKey<Block> SNOWBALL_PROVIDERS = TagKey.of(Registry.BLOCK_KEY, SNOWBALL_PROVIDERS_ID);

	private static final Identifier SNOWBALL_FIGHT_ID = new Identifier(MOD_ID, "snowball_fight");
	public static final GameType<SnowballFightConfig> SNOWBALL_FIGHT_TYPE = GameType.register(SNOWBALL_FIGHT_ID, SnowballFightConfig.CODEC, SnowballFightWaitingPhase::open);

	@Override
	public void onInitialize() {
		return;
	}
}