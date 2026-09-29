package io.github.haykam821.snowballfight;

import io.github.haykam821.snowballfight.game.SnowballFightConfig;
import io.github.haykam821.snowballfight.game.phase.SnowballFightWaitingPhase;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import xyz.nucleoid.plasmid.api.game.GameType;
import xyz.nucleoid.plasmid.api.game.GameTypes;

public class Main implements ModInitializer {
	private static final String MOD_ID = "snowballfight";

	private static final Identifier SNOWBALL_PROVIDERS_ID = Main.identifier("snowball_providers");
	public static final TagKey<Block> SNOWBALL_PROVIDERS = TagKey.create(Registries.BLOCK, SNOWBALL_PROVIDERS_ID);

	private static final Identifier SNOWBALL_FIGHT_ID = Main.identifier("snowball_fight");
	public static final GameType<SnowballFightConfig> SNOWBALL_FIGHT_TYPE = GameTypes.register(SNOWBALL_FIGHT_ID, SnowballFightConfig.CODEC, SnowballFightWaitingPhase::open);

	@Override
	public void onInitialize() {
		return;
	}

	public static Identifier identifier(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}