package io.github.haykam821.snowballfight.game.phase;

import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.Sets;

import io.github.haykam821.snowballfight.Main;
import io.github.haykam821.snowballfight.game.SnowballFightConfig;
import io.github.haykam821.snowballfight.game.map.SnowballFightMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowBlock;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.block.BlockUseEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;
import xyz.nucleoid.stimuli.event.projectile.ProjectileHitEvent;

public class SnowballFightActivePhase {
	private static final BlockState AIR_STATE = Blocks.AIR.getDefaultState();
	private static final BlockState SNOW_STATE = Blocks.SNOW.getDefaultState();

	private final ServerWorld world;
	private final GameSpace gameSpace;
	private final SnowballFightMap map;
	private final SnowballFightConfig config;
	private final Set<ServerPlayerEntity> players;
	private boolean singleplayer;
	private boolean opened;

	public SnowballFightActivePhase(GameSpace gameSpace, ServerWorld world, SnowballFightMap map, SnowballFightConfig config, Set<ServerPlayerEntity> players) {
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

	public static void open(GameSpace gameSpace, ServerWorld world, SnowballFightMap map, SnowballFightConfig config) {
		SnowballFightActivePhase phase = new SnowballFightActivePhase(gameSpace, world, map, config, Sets.newHashSet(gameSpace.getPlayers()));

		gameSpace.setActivity(activity -> {
			SnowballFightActivePhase.setRules(activity);

			// Listeners
			activity.listen(GameActivityEvents.ENABLE, phase::enable);
			activity.listen(GameActivityEvents.TICK, phase::tick);
			activity.listen(GamePlayerEvents.ADD, phase::addPlayer);
			activity.listen(GamePlayerEvents.REMOVE, phase::removePlayer);
			activity.listen(PlayerDeathEvent.EVENT, phase::onPlayerDeath);
			activity.listen(ProjectileHitEvent.ENTITY, phase::onEntityHit);
			activity.listen(BlockUseEvent.EVENT, phase::useBlock);
		});
	}

	private void enable() {
		this.opened = true;
		this.singleplayer = this.players.size() == 1;

 		for (ServerPlayerEntity player : this.players) {
			player.changeGameMode(GameMode.ADVENTURE);
		}
	}

	private BlockPos getTopSnowProviderPos(BlockPos initialPos) {
		BlockPos.Mutable pos = initialPos.mutableCopy();

		while (this.world.getBlockState(pos).isIn(Main.SNOWBALL_PROVIDERS)) {
			pos.move(Direction.UP);
		}

		return pos.down();
	}

	private ActionResult useBlock(ServerPlayerEntity player, Hand hand, BlockHitResult hitResult) {
		if (hand != Hand.MAIN_HAND) return ActionResult.PASS;
		if (!this.players.contains(player)) return ActionResult.PASS;

		BlockPos initialPos = hitResult.getBlockPos();

		BlockState initialState = this.world.getBlockState(initialPos);
		if (!initialState.isIn(Main.SNOWBALL_PROVIDERS)) return ActionResult.FAIL;

		BlockPos pos = this.getTopSnowProviderPos(initialPos);
		BlockState state = this.world.getBlockState(pos);

		if (state.getBlock() instanceof SnowBlock) {
			int currentLayers = state.get(Properties.LAYERS);
			if (currentLayers == 1) {
				this.world.setBlockState(pos, AIR_STATE);
			} else {
				this.world.setBlockState(pos, SNOW_STATE.with(Properties.LAYERS, currentLayers - 1));
			}
		} else {
			this.world.setBlockState(pos, SNOW_STATE.with(Properties.LAYERS, 7));
		}

		int snowballs = player.getInventory().count(this.config.getSnowballStack().getItem());
		if (snowballs < 16) {
			player.giveItemStack(this.config.getSnowballStack().copy());
		}

		player.playSound(SoundEvents.BLOCK_SNOW_BREAK, SoundCategory.BLOCKS, 1, 1);
		return ActionResult.SUCCESS;
	}

	private void tick() {
		Iterator<ServerPlayerEntity> playerIterator = this.players.iterator();
		while (playerIterator.hasNext()) {
			ServerPlayerEntity player = playerIterator.next();

			// Eliminate players that are outside of the arena
			if (!this.map.getBox().contains(player.getPos())) {
				this.eliminate(player, false);
				playerIterator.remove();
			}
		}

		if (this.players.size() < 2) {
			if (this.players.size() == 1 && this.singleplayer) return;

			Text endingMessage = this.getEndingMessage();
			this.gameSpace.getPlayers().sendMessage(endingMessage);

			this.gameSpace.close(GameCloseReason.FINISHED);
		}
	}

	private Text getEndingMessage() {
		if (this.players.size() == 1) {
			PlayerEntity winner = this.players.iterator().next();
			return new TranslatableText("text.snowballfight.win", winner.getDisplayName()).formatted(Formatting.GOLD);
		}
		return new TranslatableText("text.snowballfight.no_winners").formatted(Formatting.GOLD);
	}

	private void setSpectator(ServerPlayerEntity player) {
		player.changeGameMode(GameMode.SPECTATOR);
	}

	private void addPlayer(ServerPlayerEntity player) {
		if (!this.players.contains(player)) {
			this.setSpectator(player);
		} else if (this.opened) {
			this.eliminate(player, true);
		}
	}

	private void removePlayer(PlayerEntity player) {
		this.players.remove(player);
	}

	private ActionResult onPlayerHitBySnowball(SnowballEntity snowball, ServerPlayerEntity player) {
		if (snowball.getOwner().equals(player)) return ActionResult.FAIL;

		this.eliminate(player, true);
		return ActionResult.SUCCESS;
	}

	private ActionResult onEntityHit(ProjectileEntity projectile, EntityHitResult hitResult) {
		if (projectile instanceof SnowballEntity && hitResult.getEntity() instanceof ServerPlayerEntity) {
			return this.onPlayerHitBySnowball((SnowballEntity) projectile, (ServerPlayerEntity) hitResult.getEntity());
		}
		return ActionResult.PASS;
	}

	private void eliminate(ServerPlayerEntity eliminatedPlayer, boolean remove) {
		Text message = new TranslatableText("text.snowballfight.eliminated", eliminatedPlayer.getDisplayName()).formatted(Formatting.RED);
		this.gameSpace.getPlayers().sendMessage(message);

		if (remove) {
			this.players.remove(eliminatedPlayer);
		}
		this.setSpectator(eliminatedPlayer);
	}

	private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
		this.eliminate(player, true);
		return ActionResult.SUCCESS;
	}
}
