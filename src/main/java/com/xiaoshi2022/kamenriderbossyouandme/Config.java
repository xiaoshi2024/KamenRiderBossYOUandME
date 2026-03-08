package com.xiaoshi2022.kamenriderbossyouandme;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue SKILL_EXPLODE_GRIEF;
    public static final ModConfigSpec.IntValue SKILL_TOLERANCE_TIME;
    public static final ModConfigSpec.IntValue RIDER_SOUNDS_VOLUME;
    public static final ModConfigSpec.BooleanValue DEBUG_MODE;

    static {
        SKILL_EXPLODE_GRIEF = BUILDER
                .comment("技能（如骑士踢）爆炸是否摧毁方块")
                .define("skillExplosionGrief", false);
        SKILL_TOLERANCE_TIME = BUILDER
                .comment("技能额外容错（释放后额外有效/实际持续）时间（秒）")
                .defineInRange("skillToleranceTime", 1, 0, 2);
        RIDER_SOUNDS_VOLUME = BUILDER
                .comment("变身时音量大小")
                .defineInRange("riderSoundsVolume", 100, 1, 100);
        DEBUG_MODE = BUILDER
                .comment("开发者向Debug模式")
                .define("debugMode", false);
    }

    static final ModConfigSpec SPEC = BUILDER.build();
}
