package io.github.haykam821.snowballfight.game.phase;

import java.util.concurrent.CompletableFuture;

import io.github.haykam821.snowballfight.game.SnowballFightConfig;
import io.github.haykam821.snowballfight.game.map.SnowballFightMap;
import io.github.haykam821.snowballfight.game.map.SnowballFightMapBuilder;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameWorld;
import xyz.nucleoid.plasmid.game.StartResult;
import xyz.nucleoid.plasmid.game.config.PlayerConfig;
import xyz.nucleoid.plasmid.game.event.OfferPlayerListener;
import xyz.nucleoid.plasmid.game.event.PlayerAddListener;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.event.RequestStartListener;
import xyz.nucleoid.plasmid.game.player.JoinResult;
import xyz.nucleoid.plasmid.world.bubble.BubbleWorldConfig;

public class SnowballFightWaitingPhase {
	private final GameWorld gameWorld;
	private final SnowballFightMap map;
	private final SnowballFightConfig config;

	public SnowballFightWaitingPhase(GameWorld gameWorld, SnowballFightMap map, SnowballFightConfig config) {
		this.gameWorld = gameWorld;
		this.map = map;
		this.config = config;
	}

	public static CompletableFuture<GameWorld> open(GameOpenContext<SnowballFightConfig> context) {
		SnowballFightMapBuilder mapBuilder = new SnowballFightMapBuilder(context.getConfig());

		return mapBuilder.create().thenCompose(map -> {
			BubbleWorldConfig worldConfig = new BubbleWorldConfig()
				.setGenerator(map.createGenerator(context.getServer()))
				.setDefaultGameMode(GameMode.ADVENTURE);

			return context.openWorld(worldConfig).thenApply(gameWorld -> {
				SnowballFightWaitingPhase phase = new SnowballFightWaitingPhase(gameWorld, map, context.getConfig());

				gameWorld.openGame(game -> {
					SnowballFightActivePhase.setRules(game);

					// Listeners
					game.on(PlayerAddListener.EVENT, phase::addPlayer);
					game.on(PlayerDeathListener.EVENT, phase::onPlayerDeath);
					game.on(OfferPlayerListener.EVENT, phase::offerPlayer);
					game.on(RequestStartListener.EVENT, phase::requestStart);
				});

				return gameWorld;
			});
		});
	}

	private boolean isFull() {
		return this.gameWorld.getPlayerCount() >= this.config.getPlayerConfig().getMaxPlayers();
	}

	private JoinResult offerPlayer(ServerPlayerEntity player) {
		return this.isFull() ? JoinResult.gameFull() : JoinResult.ok();
	}

	private StartResult requestStart() {
		PlayerConfig playerConfig = this.config.getPlayerConfig();
		if (this.gameWorld.getPlayerCount() < playerConfig.getMinPlayers()) {
			return StartResult.NOT_ENOUGH_PLAYERS;
		}

		SnowballFightActivePhase.open(this.gameWorld, this.map, this.config);
		return StartResult.OK;
	}

	private void addPlayer(ServerPlayerEntity player) {
		this.spawn(player);
	}

	private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
		// Respawn player
		this.spawn(player);
		return ActionResult.SUCCESS;
	}

	private void spawn(ServerPlayerEntity player) {
		Vec3d center = this.map.getPlatform().getCenter();
		int fortressHeight = this.config.getMapConfig().getFortressConfig().getHeight();

		player.teleport(this.gameWorld.getWorld(), center.getX(), fortressHeight + 1, center.getZ(), 0, 0);
	}
}