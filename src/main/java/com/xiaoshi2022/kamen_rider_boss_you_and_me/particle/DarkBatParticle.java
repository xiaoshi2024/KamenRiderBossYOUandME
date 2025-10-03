package com.xiaoshi2022.kamen_rider_boss_you_and_me.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class DarkBatParticle extends TextureSheetParticle {

	/* ========== 环绕参数 ========== */
	private final double orbitRadius;   // 环绕半径
	private final double orbitSpeed;    // 每 tick 角速度（弧度）
	private double orbitAngle;          // 当前角度（弧度）

	/* 精灵图集 */
	private final SpriteSet spriteSet;

	/* ============== Provider ============== */
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet spriteSet;
		public Provider(SpriteSet spriteSet) { this.spriteSet = spriteSet; }

		@Override
		public @Nullable Particle createParticle(SimpleParticleType type, ClientLevel level,
												 double x, double y, double z,
												 double xSpeed, double ySpeed, double zSpeed) {
			// xSpeed 作为半径，ySpeed 作为角速度（可选）
			return new DarkBatParticle(level, x, y, z, xSpeed, ySpeed, spriteSet);
		}
	}

	/* ============== 构造器 ============== */
	public DarkBatParticle(ClientLevel level,
						   double x, double y, double z,
						   double radiusRaw, double speedRaw,
						   SpriteSet spriteSet) {
		super(level, 0, 0, 0);   // 后面会重新计算真实坐标
		this.spriteSet = spriteSet;

		/* ---- 视觉 ---- */
		this.setSprite(spriteSet.get(0, 1));
		this.setSize(0.1f, 0.1f);
		this.quadSize = 0.2f;
		this.lifetime = 60;        // 3 秒
		this.gravity = 0;
		this.hasPhysics = false;

		/* ---- 环绕 ---- */
		this.orbitRadius = radiusRaw == 0 ? 0.8 : radiusRaw;        // 默认半径 0.8
		this.orbitSpeed  = (speedRaw == 0 ? 0.15 : speedRaw) * (random.nextBoolean() ? 1 : -1); // 方向随机
		this.orbitAngle  = random.nextFloat() * Math.PI * 2;        // 随机起点

		/* 初始摆位 */
		updatePosition(true);
	}

	/* ============== Tick ============== */
	@Override
	public void tick() {
		super.tick();
		if (Minecraft.getInstance().player == null) {
			this.remove();
			return;
		}
		this.orbitAngle += orbitSpeed;
		updatePosition(false);
	}

	/* 根据当前角度重新计算坐标 */
	private void updatePosition(boolean initial) {
		Player player = Minecraft.getInstance().player;
		if (player == null) return;

		Vec3 center = player.position().add(0, player.getBbHeight() * 0.75, 0); // 躯干中心

		double x = center.x + orbitRadius * Math.cos(orbitAngle);
		double y = center.y;                                      // 水平环绕，若想三维可加上 sin 垂直
		double z = center.z + orbitRadius * Math.sin(orbitAngle);

		if (initial) {
			this.setPos(x, y, z);
		} else {
			this.xo = this.x;
			this.yo = this.y;
			this.zo = this.z;
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}
}