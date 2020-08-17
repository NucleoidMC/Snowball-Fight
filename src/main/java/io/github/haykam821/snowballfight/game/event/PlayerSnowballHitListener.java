package io.github.haykam821.snowballfight.game.event;

import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.EntityHitResult;
import xyz.nucleoid.plasmid.game.event.EventType;

public interface PlayerSnowballHitListener {
	EventType<PlayerSnowballHitListener> EVENT = EventType.create(PlayerSnowballHitListener.class, listeners -> {
		return (hitResult) -> {
			for (PlayerSnowballHitListener listener : listeners) {
				ActionResult result = listener.onPlayerHitBySnowball(hitResult);
				if (result != ActionResult.PASS) {
					return result;
				}
			}
			return ActionResult.PASS;
		};
	});

	ActionResult onPlayerHitBySnowball(EntityHitResult hitResult);
}
