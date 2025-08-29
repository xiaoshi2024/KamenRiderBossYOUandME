package com.xiaoshi2022.kamen_rider_boss_you_and_me.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DarkBatParticle extends TextureSheetParticle {
	// 粒子参数
	private final Vec3 dir;           // 飞行方向
	private final double speed = 1.2; // 每 tick 前进速度
	private final double maxDist = 2.0; // 最大飞行距离
	private final float angularVelocity; // 旋转速度
	private double traveled;          // 已飞距离
	private final double orbitRadius; // 环绕半径
	private final double orbitSpeed;  // 环绕速度
	private double orbitAngle;        // 环绕角度
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
			return new DarkBatParticle(world, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
		}
	}

	// 构造器
	protected DarkBatParticle(ClientLevel world,
				 double x, double y, double z,
				 double xSpeed, double ySpeed, double zSpeed,
				 SpriteSet spriteSet) {
		super(world, x, y, z, xSpeed, ySpeed, zSpeed);
		this.spriteSet = spriteSet;

		// 粒子设置 - 更小的尺寸
		this.setSprite(this.spriteSet.get(0, 1)); // 使用第一帧精灵
		this.setSize(0.1f, 0.1f);   // 碰撞箱大小 - 很小
		this.quadSize = 0.2f;       // 渲染大小 - 小
		this.lifetime = 10;         // 生命周期(ticks) - 稍长
		this.gravity = 0;           // 无重力
		this.hasPhysics = false;    // 无物理

		// 方向处理
		this.dir = new Vec3(xSpeed, ySpeed, zSpeed).normalize();
		this.traveled = 0;

		// 环绕参数 - 实现密集环绕效果
		this.orbitRadius = 0.3 + (Math.random() * 0.2); // 环绕半径 (0.3-0.5)
		this.orbitSpeed = (Math.random() - 0.5) * 0.4;   // 环绕速度 (-0.2-0.2)
		this.orbitAngle = Math.random() * Math.PI * 2;   // 初始角度随机

		// 可选：设置初始旋转
		this.roll = (float)(Math.random() * Math.PI * 2);
		this.angularVelocity = (float)((Math.random() - 0.5) * 0.2);
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

		// 更新环绕角度
		this.orbitAngle += this.orbitSpeed;

		// 计算环绕位置 - 在主方向的基础上添加环绕偏移
		// 使用球面坐标系计算环绕位置，确保粒子在三维空间中环绕
		double orbitX = Math.cos(this.orbitAngle) * this.orbitRadius;
		double orbitY = Math.sin(this.orbitAngle) * this.orbitRadius;
		double orbitZ = Math.sin(this.orbitAngle * 0.5) * this.orbitRadius * 0.5; // 增加Z轴分量，实现螺旋效果

		// 应用主方向移动和环绕移动
		this.x += dir.x * step + orbitX * 0.5;
		this.y += dir.y * step + orbitY * 0.5;
		this.z += dir.z * step + orbitZ * 0.5;
		this.traveled += step;

		// 更新旋转
		this.roll += this.angularVelocity;

		// 飞出距离后移除
		if (traveled >= maxDist) {
			this.remove();
		}
	}
}