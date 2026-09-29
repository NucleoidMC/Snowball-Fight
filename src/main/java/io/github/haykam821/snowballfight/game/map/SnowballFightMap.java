package io.github.haykam821.snowballfight.game.map;

import java.util.Set;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.api.game.level.generator.TemplateChunkGenerator;

public class SnowballFightMap {
	private final MapTemplate template;
	private final BlockBounds platform;
	private final AABB box;
	private final Vec3 spawnPos;

	public SnowballFightMap(MapTemplate template, BlockBounds platform, int fortressHeight) {
		this.template = template;
		this.platform = platform;
		this.box = this.platform.asBox().inflate(-1, 0, -1);

		Vec3 center = this.platform.center();
		this.spawnPos = new Vec3(center.x(), fortressHeight + 1, center.z());
	}

	public BlockBounds getPlatform() {
		return this.platform;
	}

	public AABB getBox() {
		return this.box;
	}

	public Vec3 getSpawnPos() {
		return this.spawnPos;
	}

	public void spawn(ServerPlayer player, ServerLevel world) {
		Vec3 pos = this.getSpawnPos();
		player.teleportTo(world, pos.x(), pos.y(), pos.z(), Set.of(), 0, 0, true);
	}

	public ChunkGenerator createGenerator(MinecraftServer server) {
		return new TemplateChunkGenerator(server, this.template);
	}
}