package com.xiaoshi2022.kamenriderbossyouandme.riders;

import com.jpigeon.ridebattlelib.server.system.SkillSystem;
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

    // ==================== Tyrant专用技能 ====================
    public static final ResourceLocation TYRANT_KICK =
            ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "tyrant_kick");
    public static final ResourceLocation TYRANT_INTANGIBILITY =
            ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "tyrant_intangibility");
    public static final ResourceLocation TYRANT_PHASE_TELEPORT =
            ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "tyrant_phase_teleport");

    // ==================== Blood专用技能 ====================
    public static final ResourceLocation BLOOD_WAVE =
            ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "blood_wave");
    public static final ResourceLocation BLOOD_BARRIER =
            ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "blood_barrier");
    public static final ResourceLocation BLOOD_GRAVITY_COLLAPSE =
            ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "blood_gravity_collapse");

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

        // ===== Tyrant专用技能标签 =====
        SKILL_TAGS.put(TYRANT_KICK, "skill_tyrant_kick");
        SKILL_TAGS.put(TYRANT_INTANGIBILITY, "skill_tyrant_intangibility");
        SKILL_TAGS.put(TYRANT_PHASE_TELEPORT, "skill_tyrant_phase_teleport");

        // ===== Blood专用技能标签 =====
        SKILL_TAGS.put(BLOOD_WAVE, "skill_blood_wave");
        SKILL_TAGS.put(BLOOD_BARRIER, "skill_blood_barrier");
        SKILL_TAGS.put(BLOOD_GRAVITY_COLLAPSE, "skill_blood_gravity_collapse");
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

        // ===== 注册Tyrant专用技能 =====

        // 注册Tyrant骑士踢技能（冷却时间8秒，威力极大）
        SkillSystem.registerSkill(
                TYRANT_KICK,
                Component.translatable("skill." + KamenRiderBossYOUandME.MODID + ".tyrant_kick")
                        .withStyle(ChatFormatting.DARK_RED)
                        .withStyle(ChatFormatting.BOLD),
                8
        );

        // 注册Tyrant虚化技能（冷却时间30秒）
        SkillSystem.registerSkill(
                TYRANT_INTANGIBILITY,
                Component.translatable("skill." + KamenRiderBossYOUandME.MODID + ".tyrant_intangibility")
                        .withStyle(ChatFormatting.LIGHT_PURPLE)
                        .withStyle(ChatFormatting.BOLD),
                30
        );

        // 注册Tyrant相位传送技能（冷却时间5秒）
        SkillSystem.registerSkill(
                TYRANT_PHASE_TELEPORT,
                Component.translatable("skill." + KamenRiderBossYOUandME.MODID + ".tyrant_phase_teleport")
                        .withStyle(ChatFormatting.AQUA)
                        .withStyle(ChatFormatting.BOLD),
                5
        );

        //注册血族
        SkillSystem.registerSkill(
                BLOOD_WAVE,
                Component.translatable("skill." + KamenRiderBossYOUandME.MODID + ".blood_wave")
                        .withStyle(ChatFormatting.DARK_RED)
                        .withStyle(ChatFormatting.BOLD),
                6  // 冷却6秒
        );

        SkillSystem.registerSkill(
                BLOOD_BARRIER,
                Component.translatable("skill." + KamenRiderBossYOUandME.MODID + ".blood_barrier")
                        .withStyle(ChatFormatting.LIGHT_PURPLE)
                        .withStyle(ChatFormatting.BOLD),
                20  // 冷却20秒
        );

        SkillSystem.registerSkill(
                BLOOD_GRAVITY_COLLAPSE,
                Component.translatable("skill." + KamenRiderBossYOUandME.MODID + ".blood_gravity_collapse")
                        .withStyle(ChatFormatting.DARK_PURPLE)
                        .withStyle(ChatFormatting.BOLD),
                15  // 冷却15秒
        );

        KamenRiderBossYOUandME.LOGGER.info("已注册 {} 个骑士技能 (5个通用 + 3个Brain专用 + 3个Tyrant专用)", 11);
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

        // ===== Tyrant专用技能显示名称 =====
        else if (TYRANT_KICK.equals(skillId)) {
            return Component.translatable("skill." + KamenRiderBossYOUandME.MODID + ".tyrant_kick.display");
        } else if (TYRANT_INTANGIBILITY.equals(skillId)) {
            return Component.translatable("skill." + KamenRiderBossYOUandME.MODID + ".tyrant_intangibility.display");
        } else if (TYRANT_PHASE_TELEPORT.equals(skillId)) {
            return Component.translatable("skill." + KamenRiderBossYOUandME.MODID + ".tyrant_phase_teleport.display");
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
                skillId.equals(BRAIN_KICK) ||
                // 检查Tyrant专用技能
                skillId.equals(TYRANT_KICK) ||
                skillId.equals(TYRANT_INTANGIBILITY) ||
                skillId.equals(TYRANT_PHASE_TELEPORT);
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
     * 检查是否是Tyrant专用技能
     */
    public static boolean isTyrantSkill(ResourceLocation skillId) {
        return skillId.equals(TYRANT_KICK) ||
                skillId.equals(TYRANT_INTANGIBILITY) ||
                skillId.equals(TYRANT_PHASE_TELEPORT);
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