package com.xiaoshi2022.kamen_rider_boss_you_and_me.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LemonsliceParticle extends TextureSheetParticle {
	// 粒子参数
	private final Vec3 dir;           // 飞行方向
	private final double speed = 0.8; // 每 tick 前进多少格
	private final double maxDist = 0.8; // 最大飞行距离
	private final float angularVelocity; // 旋转速度
	private double traveled;          // 已飞距离
	private final SpriteSet spriteSet; // 粒子精灵集

	// 粒子提供者
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet spriteSet;

		public Provider(SpriteSet spriteSet) {
			this.spriteSet = spriteSet;
		}

		@Override
		public Particle createParticle(SimpleParticleType type, ClientLevel world,
									   double x, double y, double z,
									   double xSpeed, double ySpeed, double zSpeed) {
			return new LemonsliceParticle(world, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
		}
	}

	// 构造器
	protected LemonsliceParticle(ClientLevel world,
								 double x, double y, double z,
								 double xSpeed, double ySpeed, double zSpeed,
								 SpriteSet spriteSet) {
		super(world, x, y, z, xSpeed, ySpeed, zSpeed);
		this.spriteSet = spriteSet;

		// 粒子设置
		this.setSprite(this.spriteSet.get(0, 1)); // 使用第一帧精灵
		this.setSize(0.6f, 1.8f);   // 碰撞箱大小
		this.quadSize = 1.8f;       // 渲染大小
		this.lifetime = 6;          // 生命周期(ticks)
		this.gravity = 0;           // 无重力
		this.hasPhysics = false;    // 无物理

		// 方向处理
		this.dir = new Vec3(xSpeed, ySpeed, zSpeed).normalize();
		this.traveled = 0;

		// 可选：设置初始旋转
		this.roll = (float)(Math.random() * Math.PI * 2);
		this.angularVelocity = (float)((Math.random() - 0.5) * 0.1);
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

	@Override
	public void tick() {
		super.tick();

		// 更新位置
		double step = speed;
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;

		this.x += dir.x * step;
		this.y += dir.y * step;
		this.z += dir.z * step;
		this.traveled += step;

		// 更新旋转
		this.roll += this.angularVelocity;

		// 飞出距离后移除
		if (traveled >= maxDist) {
			this.remove();
		}
	}
}