package com.xiaoshi2022.kamenriderbossyouandme.riders;

import com.jpigeon.ridebattlelib.core.system.skill.SkillSystem;
import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class RiderSkills {
    // ==================== 通用骑士技能 ====================
    public static final ResourceLocation RIDER_KICK =
            ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "rider_kick");
    public static final ResourceLocation RIDER_HEADBUTT =
            ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "rider_headbutt");
    public static final ResourceLocation RIDER_PUNCH =
            ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "rider_punch");
    public static final ResourceLocation RIDER_SLASH =
            ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "rider_slash");
    public static final ResourceLocation RIDER_BLAST =
            ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "rider_blast");

    // ==================== Brain专用技能 ====================
    public static final ResourceLocation BRAIN_HEADBUTT =
            ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "brain_headbutt");
    public static final ResourceLocation BRAIN_POISON =
            ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "brain_poison");
    public static final ResourceLocation BRAIN_KICK =
            ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "brain_kick");

    // 技能标签映射 - 像鸽子作者那样
    public static final Map<ResourceLocation, String> SKILL_TAGS = new HashMap<>();

    static {
        // ===== 通用技能标签 =====
        SKILL_TAGS.put(RIDER_KICK, "skill_rider_kick");
        SKILL_TAGS.put(RIDER_HEADBUTT, "skill_rider_headbutt");
        SKILL_TAGS.put(RIDER_PUNCH, "skill_rider_punch");
        SKILL_TAGS.put(RIDER_SLASH, "skill_rider_slash");
        SKILL_TAGS.put(RIDER_BLAST, "skill_rider_blast");

        // ===== Brain专用技能标签 =====
        SKILL_TAGS.put(BRAIN_HEADBUTT, "skill_brain_headbutt");
        SKILL_TAGS.put(BRAIN_POISON, "skill_brain_poison");
        SKILL_TAGS.put(BRAIN_KICK, "skill_brain_kick");
    }

    // 注册所有技能
    public static void registerSkills() {
        // ===== 注册通用骑士技能 =====

        // 注册骑士踢技能（冷却时间5秒）
        SkillSystem.registerSkill(
                RIDER_KICK,
                Component.translatable("skill." + KamenRiderBossYOUandME.MODID + ".rider_kick")
                        .withStyle(ChatFormatting.GOLD)
                        .withStyle(ChatFormatting.BOLD),
                5
        );

        // 注册头槌技能（冷却时间3秒）
        SkillSystem.registerSkill(
                RIDER_HEADBUTT,
                Component.translatable("skill." + KamenRiderBossYOUandME.MODID + ".rider_headbutt")
                        .withStyle(ChatFormatting.RED)
                        .withStyle(ChatFormatting.BOLD),
                3
        );

        // 注册骑士拳技能（冷却时间2秒）
        SkillSystem.registerSkill(
                RIDER_PUNCH,
                Component.translatable("skill." + KamenRiderBossYOUandME.MODID + ".rider_punch")
                        .withStyle(ChatFormatting.WHITE)
                        .withStyle(ChatFormatting.BOLD),
                2
        );

        // 注册骑士斩技能（冷却时间4秒）
        SkillSystem.registerSkill(
                RIDER_SLASH,
                Component.translatable("skill." + KamenRiderBossYOUandME.MODID + ".rider_slash")
                        .withStyle(ChatFormatting.AQUA)
                        .withStyle(ChatFormatting.BOLD),
                4
        );

        // 注册骑士爆破技能（冷却时间8秒）
        SkillSystem.registerSkill(
                RIDER_BLAST,
                Component.translatable("skill." + KamenRiderBossYOUandME.MODID + ".rider_blast")
                        .withStyle(ChatFormatting.LIGHT_PURPLE)
                        .withStyle(ChatFormatting.BOLD)
                        .withStyle(ChatFormatting.ITALIC),
                8
        );

        // ===== 注册Brain专用技能 =====

        // 注册Brain头槌技能（冷却时间3秒）
        SkillSystem.registerSkill(
                BRAIN_HEADBUTT,
                Component.translatable("skill." + KamenRiderBossYOUandME.MODID + ".brain_headbutt")
                        .withStyle(ChatFormatting.RED)
                        .withStyle(ChatFormatting.BOLD),
                3
        );

        // 注册Brain毒手技能（冷却时间8秒）
        SkillSystem.registerSkill(
                BRAIN_POISON,
                Component.translatable("skill." + KamenRiderBossYOUandME.MODID + ".brain_poison")
                        .withStyle(ChatFormatting.DARK_PURPLE)
                        .withStyle(ChatFormatting.BOLD),
                8
        );

        // 注册Brain骑士踢技能（冷却时间6秒）
        SkillSystem.registerSkill(
                BRAIN_KICK,
                Component.translatable("skill." + KamenRiderBossYOUandME.MODID + ".brain_kick")
                        .withStyle(ChatFormatting.GOLD)
                        .withStyle(ChatFormatting.BOLD),
                6
        );

        KamenRiderBossYOUandME.LOGGER.info("已注册 {} 个骑士技能 (5个通用 + 3个Brain专用)", 8);
    }

    /**
     * 获取技能的中文显示名称（用于客户端显示）
     */
    public static Component getSkillDisplayName(ResourceLocation skillId) {
        // ===== 通用技能显示名称 =====
        if (RIDER_KICK.equals(skillId)) {
            return Component.translatable("skill." + KamenRiderBossYOUandME.MODID + ".rider_kick.display");
        } else if (RIDER_HEADBUTT.equals(skillId)) {
            return Component.translatable("skill." + KamenRiderBossYOUandME.MODID + ".rider_headbutt.display");
        } else if (RIDER_PUNCH.equals(skillId)) {
            return Component.translatable("skill." + KamenRiderBossYOUandME.MODID + ".rider_punch.display");
        } else if (RIDER_SLASH.equals(skillId)) {
            return Component.translatable("skill." + KamenRiderBossYOUandME.MODID + ".rider_slash.display");
        } else if (RIDER_BLAST.equals(skillId)) {
            return Component.translatable("skill." + KamenRiderBossYOUandME.MODID + ".rider_blast.display");
        }

        // ===== Brain专用技能显示名称 =====
        else if (BRAIN_HEADBUTT.equals(skillId)) {
            return Component.translatable("skill." + KamenRiderBossYOUandME.MODID + ".brain_headbutt.display");
        } else if (BRAIN_POISON.equals(skillId)) {
            return Component.translatable("skill." + KamenRiderBossYOUandME.MODID + ".brain_poison.display");
        } else if (BRAIN_KICK.equals(skillId)) {
            return Component.translatable("skill." + KamenRiderBossYOUandME.MODID + ".brain_kick.display");
        }

        return Component.literal("未知技能");
    }

    /**
     * 检查技能ID是否有效
     */
    public static boolean isValidSkill(ResourceLocation skillId) {
        // 检查通用技能
        return skillId.equals(RIDER_KICK) ||
                skillId.equals(RIDER_HEADBUTT) ||
                skillId.equals(RIDER_PUNCH) ||
                skillId.equals(RIDER_SLASH) ||
                skillId.equals(RIDER_BLAST) ||
                // 检查Brain专用技能
                skillId.equals(BRAIN_HEADBUTT) ||
                skillId.equals(BRAIN_POISON) ||
                skillId.equals(BRAIN_KICK);
    }

    /**
     * 检查是否是Brain专用技能
     */
    public static boolean isBrainSkill(ResourceLocation skillId) {
        return skillId.equals(BRAIN_HEADBUTT) ||
                skillId.equals(BRAIN_POISON) ||
                skillId.equals(BRAIN_KICK);
    }

    /**
     * 检查是否是通用骑士技能
     */
    public static boolean isGenericSkill(ResourceLocation skillId) {
        return skillId.equals(RIDER_KICK) ||
                skillId.equals(RIDER_HEADBUTT) ||
                skillId.equals(RIDER_PUNCH) ||
                skillId.equals(RIDER_SLASH) ||
                skillId.equals(RIDER_BLAST);
    }
}