package io.github.haykam821.snowballfight.game.phase;

import io.github.haykam821.snowballfight.game.SnowballFightConfig;
import io.github.haykam821.snowballfight.game.map.SnowballFightMap;
import io.github.haykam821.snowballfight.game.map.SnowballFightMapBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.GameType;
import xyz.nucleoid.fantasy.RuntimeLevelConfig;
import xyz.nucleoid.plasmid.api.game.GameOpenContext;
import xyz.nucleoid.plasmid.api.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.api.game.GameResult;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptor;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptorResult;
import xyz.nucleoid.plasmid.api.game.player.JoinOffer;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public class SnowballFightWaitingPhase {
	private final GameSpace gameSpace;
	private final ServerLevel world;
	private final SnowballFightMap map;
	private final SnowballFightConfig config;

	public SnowballFightWaitingPhase(GameSpace gameSpace, ServerLevel world, SnowballFightMap map, SnowballFightConfig config) {
		this.gameSpace = gameSpace;
		this.world = world;
		this.map = map;
		this.config = config;
	}

	public static GameOpenProcedure open(GameOpenContext<SnowballFightConfig> context) {
		SnowballFightMapBuilder mapBuilder = new SnowballFightMapBuilder(context.config(), context.server().overworld().getRandom());
		SnowballFightMap map = mapBuilder.create();

		RuntimeLevelConfig worldConfig = new RuntimeLevelConfig()
			.setGenerator(map.createGenerator(context.server()));

		return context.openWithLevel(worldConfig, (activity, world) -> {
			SnowballFightWaitingPhase phase = new SnowballFightWaitingPhase(activity.getGameSpace(), world, map, context.config());

			GameWaitingLobby.addTo(activity, context.config().getPlayerConfig());
			SnowballFightActivePhase.setRules(activity);

			// Listeners
			activity.listen(GamePlayerEvents.ACCEPT, phase::onAcceptPlayers);
			activity.listen(GamePlayerEvents.OFFER, JoinOffer::accept);
			activity.listen(PlayerDeathEvent.EVENT, phase::onPlayerDeath);
			activity.listen(GameActivityEvents.REQUEST_START, phase::requestStart);
		});
	}

	private JoinAcceptorResult onAcceptPlayers(JoinAcceptor acceptor) {
		return acceptor.teleport(this.world, this.map.getSpawnPos()).thenRunForEach(player -> {
			player.setGameMode(GameType.ADVENTURE);
		});
	}

	private GameResult requestStart() {
		SnowballFightActivePhase.open(this.gameSpace, this.world, this.map, this.config);
		return GameResult.ok();
	}

	private EventResult onPlayerDeath(ServerPlayer player, DamageSource source) {
		// Respawn player
		this.map.spawn(player, this.world);
		return EventResult.ALLOW;
	}
}
