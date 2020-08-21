package io.github.haykam821.snowballfight.game.phase;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import io.github.haykam821.snowballfight.Main;
import io.github.haykam821.snowballfight.game.SnowballFightConfig;
import io.github.haykam821.snowballfight.game.event.PlayerSnowballHitListener;
import io.github.haykam821.snowballfight.game.map.SnowballFightMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowBlock;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import xyz.nucleoid.plasmid.game.Game;
import xyz.nucleoid.plasmid.game.GameWorld;
import xyz.nucleoid.plasmid.game.event.GameOpenListener;
import xyz.nucleoid.plasmid.game.event.GameTickListener;
import xyz.nucleoid.plasmid.game.event.PlayerAddListener;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.event.PlayerRemoveListener;
import xyz.nucleoid.plasmid.game.event.UseBlockListener;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

public class SnowballFightActivePhase {
	private static final BlockState AIR_STATE = Blocks.AIR.getDefaultState();
	private static final BlockState SNOW_STATE = Blocks.SNOW.getDefaultState();

	private final ServerWorld world;
	private final GameWorld gameWorld;
	private final SnowballFightMap map;
	private final SnowballFightConfig config;
	private final Set<ServerPlayerEntity> players;
	private boolean singleplayer;
	private boolean opened;

	public SnowballFightActivePhase(GameWorld gameWorld, SnowballFightMap map, SnowballFightConfig config, Set<ServerPlayerEntity> players) {
		this.world = gameWorld.getWorld();
		this.gameWorld = gameWorld;
		this.map = map;
		this.config = config;
		this.players = players;
	}

	public static void setRules(Game game) {
		game.setRule(GameRule.BLOCK_DROPS, RuleResult.DENY);
		game.setRule(GameRule.CRAFTING, RuleResult.DENY);
		game.setRule(GameRule.FALL_DAMAGE, RuleResult.DENY);
		game.setRule(GameRule.HUNGER, RuleResult.DENY);
		game.setRule(GameRule.PORTALS, RuleResult.DENY);
		game.setRule(GameRule.PVP, RuleResult.DENY);
		game.setRule(GameRule.THROW_ITEMS, RuleResult.DENY);
	}

	public static void open(GameWorld gameWorld, SnowballFightMap map, SnowballFightConfig config) {
		SnowballFightActivePhase phase = new SnowballFightActivePhase(gameWorld, map, config, new HashSet<>(gameWorld.getPlayers()));

		gameWorld.openGame(game -> {
			SnowballFightActivePhase.setRules(game);

			// Listeners
			game.on(GameOpenListener.EVENT, phase::open);
			game.on(GameTickListener.EVENT, phase::tick);
			game.on(PlayerAddListener.EVENT, phase::addPlayer);
			game.on(PlayerRemoveListener.EVENT, phase::removePlayer);
			game.on(PlayerDeathListener.EVENT, phase::onPlayerDeath);
			game.on(PlayerSnowballHitListener.EVENT, phase::onPlayerHitBySnowball);
			game.on(UseBlockListener.EVENT, phase::useBlock);
		});
	}

	private void open() {
		this.opened = true;
		this.singleplayer = this.players.size() == 1;

 		for (ServerPlayerEntity player : this.players) {
			player.setGameMode(GameMode.ADVENTURE);
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
		if (hand != Hand.MAIN_HAND)
			return ActionResult.PASS;

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

		int snowballs = player.inventory.count(this.config.getSnowballStack().getItem());
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
			this.gameWorld.getPlayerSet().sendMessage(endingMessage);

			this.gameWorld.close();
		}
	}

	private Text getEndingMessage() {
		if (this.players.size() == 1) {
			PlayerEntity winner = this.players.iterator().next();
			return winner.getDisplayName().shallowCopy().append(" has won the game!").formatted(Formatting.GOLD);
		}
		return new LiteralText("Nobody won the game!").formatted(Formatting.GOLD);
	}

	private void setSpectator(PlayerEntity player) {
		player.setGameMode(GameMode.SPECTATOR);
	}

	private void addPlayer(PlayerEntity player) {
		if (!this.players.contains(player)) {
			this.setSpectator(player);
		} else if (this.opened) {
			this.eliminate(player, true);
		}
	}

	private void removePlayer(PlayerEntity player) {
		this.players.remove(player);
	}

	private ActionResult onPlayerHitBySnowball(SnowballEntity snowball, EntityHitResult hitResult) {
		if (snowball.getOwner().equals(hitResult.getEntity())) return ActionResult.FAIL;

		this.eliminate((PlayerEntity) hitResult.getEntity(), true);
		return ActionResult.SUCCESS;
	}

	private void eliminate(PlayerEntity eliminatedPlayer, boolean remove) {
		Text message = eliminatedPlayer.getDisplayName().shallowCopy().append(" has been eliminated!").formatted(Formatting.RED);
		this.gameWorld.getPlayerSet().sendMessage(message);

		if (remove) {
			this.players.remove(eliminatedPlayer);
		}
		this.setSpectator(eliminatedPlayer);
	}

	private ActionResult onPlayerDeath(PlayerEntity player, DamageSource source) {
		this.eliminate(player, true);
		return ActionResult.SUCCESS;
	}
}
