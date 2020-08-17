package io.github.haykam821.snowballfight.game.event;

import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.EntityHitResult;
import xyz.nucleoid.plasmid.game.event.EventType;

public interface PlayerSnowballHitListener {
	EventType<PlayerSnowballHitListener> EVENT = EventType.create(PlayerSnowballHitListener.class, listeners -> {
		return (snowball, hitResult) -> {
			for (PlayerSnowballHitListener listener : listeners) {
				ActionResult result = listener.onPlayerHitBySnowball(snowball, hitResult);
				if (result != ActionResult.PASS) {
					return result;
				}
			}
			return ActionResult.PASS;
		};
	});

	ActionResult onPlayerHitBySnowball(SnowballEntity snowball, EntityHitResult hitResult);
}
