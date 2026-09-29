package io.github.haykam821.snowballfight.game.phase;

import java.util.Iterator;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import com.google.common.collect.Sets;

import io.github.haykam821.snowballfight.Main;
import io.github.haykam821.snowballfight.game.SnowballFightConfig;
import io.github.haykam821.snowballfight.game.map.SnowballFightMap;
import xyz.nucleoid.plasmid.api.game.GameActivity;
import xyz.nucleoid.plasmid.api.game.GameCloseReason;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptor;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptorResult;
import xyz.nucleoid.plasmid.api.game.player.JoinOffer;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.plasmid.api.util.PlayerUtil;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.block.BlockUseEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;
import xyz.nucleoid.stimuli.event.projectile.ProjectileHitEvent;

public class SnowballFightActivePhase {
	private static final BlockState AIR_STATE = Blocks.AIR.defaultBlockState();
	private static final BlockState SNOW_STATE = Blocks.SNOW.defaultBlockState();

	private final ServerLevel world;
	private final GameSpace gameSpace;
	private final SnowballFightMap map;
	private final SnowballFightConfig config;
	private final Set<ServerPlayer> players;
	private boolean singleplayer;
	private int ticksUntilClose = -1;

	public SnowballFightActivePhase(GameSpace gameSpace, ServerLevel world, SnowballFightMap map, SnowballFightConfig config, Set<ServerPlayer> players) {
		this.world = world;
		this.gameSpace = gameSpace;
		this.map = map;
		this.config = config;
		this.players = players;
	}

	public static void setRules(GameActivity activity) {
		activity.deny(GameRuleType.BLOCK_DROPS);
		activity.deny(GameRuleType.CRAFTING);
		activity.deny(GameRuleType.FALL_DAMAGE);
		activity.deny(GameRuleType.HUNGER);
		activity.deny(GameRuleType.PORTALS);
		activity.deny(GameRuleType.PVP);
		activity.deny(GameRuleType.THROW_ITEMS);
	}

	public static void open(GameSpace gameSpace, ServerLevel world, SnowballFightMap map, SnowballFightConfig config) {
		SnowballFightActivePhase phase = new SnowballFightActivePhase(gameSpace, world, map, config, Sets.newHashSet(gameSpace.getPlayers().participants()));

		gameSpace.setActivity(activity -> {
			SnowballFightActivePhase.setRules(activity);

			// Listeners
			activity.listen(GameActivityEvents.ENABLE, phase::enable);
			activity.listen(GameActivityEvents.TICK, phase::tick);
			activity.listen(GamePlayerEvents.ACCEPT, phase::onAcceptPlayers);
			activity.listen(GamePlayerEvents.OFFER, JoinOffer::acceptSpectators);
			activity.listen(GamePlayerEvents.REMOVE, phase::removePlayer);
			activity.listen(PlayerDeathEvent.EVENT, phase::onPlayerDeath);
			activity.listen(ProjectileHitEvent.ENTITY, phase::onEntityHit);
			activity.listen(BlockUseEvent.EVENT, phase::useBlock);
		});
	}

	private void enable() {
		this.singleplayer = this.players.size() == 1;

 		for (ServerPlayer player : this.players) {
			player.setGameMode(GameType.ADVENTURE);
		}

		for (ServerPlayer player : this.gameSpace.getPlayers().spectators()) {
			this.map.spawn(player, this.world);
			player.setGameMode(GameType.SPECTATOR);
		}
	}

	private BlockPos getTopSnowProviderPos(BlockPos initialPos) {
		BlockPos.MutableBlockPos pos = initialPos.mutable();

		while (this.world.getBlockState(pos).is(Main.SNOWBALL_PROVIDERS)) {
			pos.move(Direction.UP);
		}

		return pos.below();
	}

	private InteractionResult useBlock(ServerPlayer player, InteractionHand hand, BlockHitResult hitResult) {
		if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
		if (!this.players.contains(player)) return InteractionResult.PASS;

		BlockPos initialPos = hitResult.getBlockPos();

		BlockState initialState = this.world.getBlockState(initialPos);
		if (!initialState.is(Main.SNOWBALL_PROVIDERS)) return InteractionResult.FAIL;

		BlockPos pos = this.getTopSnowProviderPos(initialPos);
		BlockState state = this.world.getBlockState(pos);

		if (state.getBlock() instanceof SnowLayerBlock) {
			int currentLayers = state.getValue(BlockStateProperties.LAYERS);
			if (currentLayers == 1) {
				this.world.setBlockAndUpdate(pos, AIR_STATE);
			} else {
				this.world.setBlockAndUpdate(pos, SNOW_STATE.setValue(BlockStateProperties.LAYERS, currentLayers - 1));
			}
		} else {
			this.world.setBlockAndUpdate(pos, SNOW_STATE.setValue(BlockStateProperties.LAYERS, 7));
		}

		int snowballs = player.getInventory().countItem(this.config.getSnowballStack().item().value());
		if (snowballs < 16) {
			player.addItem(this.config.getSnowballStack().create());
		}

		PlayerUtil.playSoundToPlayer(player, SoundEvents.SNOW_BREAK, SoundSource.BLOCKS, 1, 1);
		return InteractionResult.SUCCESS_SERVER;
	}

	private void tick() {
		// Decrease ticks until game end to zero
		if (this.isGameEnding()) {
			if (this.ticksUntilClose == 0) {
				this.gameSpace.close(GameCloseReason.FINISHED);
			}

			this.ticksUntilClose -= 1;
			return;
		}

		Iterator<ServerPlayer> playerIterator = this.players.iterator();
		while (playerIterator.hasNext()) {
			ServerPlayer player = playerIterator.next();

			// Eliminate players that are outside of the arena
			if (!this.map.getBox().contains(player.position())) {
				this.eliminate(player, false);
				playerIterator.remove();
			}
		}

		if (this.players.size() < 2) {
			if (this.players.size() == 1 && this.singleplayer) return;

			Component endingMessage = this.getEndingMessage();
			this.gameSpace.getPlayers().sendMessage(endingMessage);

			this.ticksUntilClose = this.config.getTicksUntilClose().sample(this.world.getRandom());
		}
	}

	private boolean isGameEnding() {
		return this.ticksUntilClose >= 0;
	}

	private Component getEndingMessage() {
		if (this.players.size() == 1) {
			Player winner = this.players.iterator().next();
			return Component.translatable("text.snowballfight.win", winner.getDisplayName()).withStyle(ChatFormatting.GOLD);
		}
		return Component.translatable("text.snowballfight.no_winners").withStyle(ChatFormatting.GOLD);
	}

	private void setSpectator(ServerPlayer player) {
		player.setGameMode(GameType.SPECTATOR);
	}

	private JoinAcceptorResult onAcceptPlayers(JoinAcceptor acceptor) {
		return acceptor.teleport(this.world, this.map.getSpawnPos()).thenRunForEach(player -> {
			this.setSpectator(player);
		});
	}

	private void removePlayer(ServerPlayer player) {
		this.eliminate(player, true);
	}

	private EventResult onPlayerHitBySnowball(Snowball snowball, ServerPlayer player) {
		if (!this.config.shouldAllowSelfEliminating() && snowball.getOwner().equals(player)) return EventResult.DENY;

		Component message = this.getEliminatedMessage(".by", player.getDisplayName(), snowball.getOwner().getDisplayName());
		this.eliminate(player, true, message);

		return EventResult.ALLOW;
	}

	private EventResult onEntityHit(Projectile projectile, EntityHitResult hitResult) {
		if (projectile instanceof Snowball && hitResult.getEntity() instanceof ServerPlayer) {
			return this.onPlayerHitBySnowball((Snowball) projectile, (ServerPlayer) hitResult.getEntity());
		}
		return EventResult.PASS;
	}

	private void eliminate(ServerPlayer eliminatedPlayer, boolean remove, Component message) {
		if (this.isGameEnding()) return;
		if (!this.players.contains(eliminatedPlayer)) return;

		this.gameSpace.getPlayers().sendMessage(message);

		if (remove) {
			this.players.remove(eliminatedPlayer);
		}
		this.setSpectator(eliminatedPlayer);
	}

	private void eliminate(ServerPlayer eliminatedPlayer, boolean remove) {
		Component message = this.getEliminatedMessage("", eliminatedPlayer.getDisplayName());
		this.eliminate(eliminatedPlayer, remove, message);
	}

	private Component getEliminatedMessage(String suffix, Object... args) {
		return Component.translatable("text.snowballfight.eliminated" + suffix, args).withStyle(ChatFormatting.RED);
	}

	private EventResult onPlayerDeath(ServerPlayer player, DamageSource source) {
		this.eliminate(player, true);
		return EventResult.ALLOW;
	}
}
