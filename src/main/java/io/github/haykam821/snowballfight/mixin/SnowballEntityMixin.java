package io.github.haykam821.snowballfight.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.haykam821.snowballfight.game.event.PlayerSnowballHitListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import xyz.nucleoid.plasmid.game.GameWorld;

@Mixin(SnowballEntity.class)
public class SnowballEntityMixin {
	@Inject(method = "onEntityHit", at = @At("HEAD"), cancellable = true)
	private void invokePlayerSnowballHitListener(EntityHitResult hitResult, CallbackInfo ci) {
		Entity entity = hitResult.getEntity();

		World world = entity.getEntityWorld();
		GameWorld gameWorld = GameWorld.forWorld(world);
		if (gameWorld == null) return;

		if (!(entity instanceof ServerPlayerEntity)) return;
		ServerPlayerEntity player = (ServerPlayerEntity) entity;
		if (!gameWorld.containsPlayer(player)) return;

		ActionResult result = gameWorld.invoker(PlayerSnowballHitListener.EVENT).onPlayerHitBySnowball(hitResult);
		if (result == ActionResult.FAIL) {
			ci.cancel();
		}
	}
}