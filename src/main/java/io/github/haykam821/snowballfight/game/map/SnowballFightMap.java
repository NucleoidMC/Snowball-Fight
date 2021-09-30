package io.github.haykam821.snowballfight.game.map;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.game.world.generator.TemplateChunkGenerator;

public class SnowballFightMap {
	private final MapTemplate template;
	private final BlockBounds platform;
	private final Box box;
	private final Vec3d spawnPos;

	public SnowballFightMap(MapTemplate template, BlockBounds platform, int fortressHeight) {
		this.template = template;
		this.platform = platform;
		this.box = this.platform.asBox().expand(-1, 0, -1);

		Vec3d center = this.platform.center();
		this.spawnPos = new Vec3d(center.getX(), fortressHeight + 1, center.getZ());
	}

	public BlockBounds getPlatform() {
		return this.platform;
	}

	public Box getBox() {
		return this.box;
	}

	public Vec3d getSpawnPos() {
		return this.spawnPos;
	}

	public ChunkGenerator createGenerator(MinecraftServer server) {
		return new TemplateChunkGenerator(server, this.template);
	}
}