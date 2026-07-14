package com.xiaoshi2022.kamenriderbossyouandme.registry;

import com.xiaoshi2022.kamenriderbossyouandme.Accessory.BrainDriver;
import com.xiaoshi2022.kamenriderbossyouandme.Accessory.BuildDriver;
import com.xiaoshi2022.kamenriderbossyouandme.Accessory.Genesis_driver;
import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import com.xiaoshi2022.kamenriderbossyouandme.items.prop.Cobra;
import com.xiaoshi2022.kamenriderbossyouandme.items.prop.Dragonfruit;
import com.xiaoshi2022.kamenriderbossyouandme.items.prop.GreatDragon;
import com.xiaoshi2022.kamenriderbossyouandme.items.prop.HazardTrigger;
import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.DarkRiderGhost.DarkRiderGhost;
import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.NapoleonGhost.NapoleonGhost;
import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.baron_lemon.BaronLemon;
import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.blackbuild.BlackBuild;
import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.blood.Blood;
import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.brain.Brain;
import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.darkKiva.DarkKiva;
import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.dark_orangels.DarkOrangels;
import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.duke.Duke;
import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.evilbats.EvilBats;
import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.marika.Marika;
import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.noxknight.NoxKnight;
import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.quinbee.Quinbee;
import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.rider_barons.RiderBarons;
import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.rider_necrom.RiderNecrom;
import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.sigurd.Sigurd;
import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.tyrant.Tyrant;
import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.zangetsu_shin.zangetsuShin;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(KamenRiderBossYOUandME.MODID);

    // 脑骑
    public static final DeferredItem<Item> BRAIN_DRIVER = ITEMS.register("brain_driver",
                () -> new BrainDriver(new Item.Properties().stacksTo(1)));

    // 创世纪驱动器
    public static final DeferredItem<Item> GENESIS_DRIVER = ITEMS.register("genesis_driver",
            () -> new Genesis_driver(new Item.Properties().stacksTo(1)));

    // Build驱动器
    public static final DeferredItem<Item> BUILD_DRIVER = ITEMS.register("build_driver",
            () -> new BuildDriver(new Item.Properties().stacksTo(1)));

    // 危险扳机
    public static final DeferredItem<Item> HAZARD_TRIGGER = ITEMS.register("hazard_trigger",
            () -> new HazardTrigger(new Item.Properties().stacksTo(1)));

    // 火龙果锁种
    public static final DeferredItem<Item> DRAGONFRUIT = ITEMS.register("dragonfruit",
            () -> new Dragonfruit(new Item.Properties().stacksTo(1)));

    // 眼镜蛇满瓶
    public static final DeferredItem<Item> COBRA = ITEMS.register("cobra",
            () -> new Cobra(new Item.Properties().stacksTo(1)));

    // 伟大龙满瓶
    public static final DeferredItem<Item> GREAT_DRAGON = ITEMS.register("great_dragon",
            () -> new GreatDragon(new Item.Properties().stacksTo(1)));


    // 装甲物品 - 使用原版的 ArmorMaterials

    // Blood - 假面骑士Blood
    public static final DeferredItem<Item> BLOOD_HELMET = ITEMS.register("blood_helmet",
            () -> new Blood(ArmorMaterials.DIAMOND, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final DeferredItem<Item> BLOOD_CHESTPLATE = ITEMS.register("blood_chestplate",
            () -> new Blood(ArmorMaterials.DIAMOND, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final DeferredItem<Item> BLOOD_LEGGINGS = ITEMS.register("blood_leggings",
            () -> new Blood(ArmorMaterials.DIAMOND, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    
    // Baron Lemon
    public static final DeferredItem<Item> BARON_LEMON_HELMET = ITEMS.register("baron_lemon_helmet",
            () -> new BaronLemon(ArmorMaterials.DIAMOND, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final DeferredItem<Item> BARON_LEMON_CHESTPLATE = ITEMS.register("baron_lemon_chestplate",
            () -> new BaronLemon(ArmorMaterials.DIAMOND, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final DeferredItem<Item> BARON_LEMON_LEGGINGS = ITEMS.register("baron_lemon_leggings",
            () -> new BaronLemon(ArmorMaterials.DIAMOND, ArmorItem.Type.LEGGINGS, new Item.Properties()));

    // Black Build
    public static final DeferredItem<Item> BLACK_BUILD_HELMET = ITEMS.register("black_build_helmet",
            () -> new BlackBuild(ArmorMaterials.NETHERITE, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final DeferredItem<Item> BLACK_BUILD_CHESTPLATE = ITEMS.register("black_build_chestplate",
            () -> new BlackBuild(ArmorMaterials.NETHERITE, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final DeferredItem<Item> BLACK_BUILD_LEGGINGS = ITEMS.register("black_build_leggings",
            () -> new BlackBuild(ArmorMaterials.NETHERITE, ArmorItem.Type.LEGGINGS, new Item.Properties()));

    // Brain
    public static final DeferredItem<Item> BRAIN_HELMET = ITEMS.register("brain_helmet",
            () -> new Brain(ModArmorMaterials.BRAIN, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final DeferredItem<Item> BRAIN_CHESTPLATE = ITEMS.register("brain_chestplate",
            () -> new Brain(ModArmorMaterials.BRAIN, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final DeferredItem<Item> BRAIN_LEGGINGS = ITEMS.register("brain_leggings",
            () -> new Brain(ModArmorMaterials.BRAIN, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final DeferredItem<Item> BRAIN_BOOTS = ITEMS.register("brain_boots",
            () -> new Brain(ModArmorMaterials.BRAIN, ArmorItem.Type.BOOTS, new Item.Properties()));

    // Dark Orangels
    public static final DeferredItem<Item> DARK_ORANGELS_HELMET = ITEMS.register("dark_orangels_helmet",
            () -> new DarkOrangels(ArmorMaterials.GOLD, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final DeferredItem<Item> DARK_ORANGELS_CHESTPLATE = ITEMS.register("dark_orangels_chestplate",
            () -> new DarkOrangels(ArmorMaterials.GOLD, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final DeferredItem<Item> DARK_ORANGELS_LEGGINGS = ITEMS.register("dark_orangels_leggings",
            () -> new DarkOrangels(ArmorMaterials.GOLD, ArmorItem.Type.LEGGINGS, new Item.Properties()));

    // Dark Kiva
    public static final DeferredItem<Item> DARK_KIVA_HELMET = ITEMS.register("dark_kiva_helmet",
            () -> new DarkKiva(ArmorMaterials.NETHERITE, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final DeferredItem<Item> DARK_KIVA_CHESTPLATE = ITEMS.register("dark_kiva_chestplate",
            () -> new DarkKiva(ArmorMaterials.NETHERITE, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final DeferredItem<Item> DARK_KIVA_LEGGINGS = ITEMS.register("dark_kiva_leggings",
            () -> new DarkKiva(ArmorMaterials.NETHERITE, ArmorItem.Type.LEGGINGS, new Item.Properties()));

    // Dark Rider Ghost
    public static final DeferredItem<Item> DARK_RIDER_GHOST_HELMET = ITEMS.register("dark_rider_ghost_helmet",
            () -> new DarkRiderGhost(ArmorMaterials.DIAMOND, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final DeferredItem<Item> DARK_RIDER_GHOST_CHESTPLATE = ITEMS.register("dark_rider_ghost_chestplate",
            () -> new DarkRiderGhost(ArmorMaterials.DIAMOND, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final DeferredItem<Item> DARK_RIDER_GHOST_LEGGINGS = ITEMS.register("dark_rider_ghost_leggings",
            () -> new DarkRiderGhost(ArmorMaterials.DIAMOND, ArmorItem.Type.LEGGINGS, new Item.Properties()));

    // Duke
    public static final DeferredItem<Item> DUKE_HELMET = ITEMS.register("duke_helmet",
            () -> new Duke(ArmorMaterials.IRON, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final DeferredItem<Item> DUKE_CHESTPLATE = ITEMS.register("duke_chestplate",
            () -> new Duke(ArmorMaterials.IRON, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final DeferredItem<Item> DUKE_LEGGINGS = ITEMS.register("duke_leggings",
            () -> new Duke(ArmorMaterials.IRON, ArmorItem.Type.LEGGINGS, new Item.Properties()));

    // Evil Bats
    public static final DeferredItem<Item> EVIL_BATS_HELMET = ITEMS.register("evil_bats_helmet",
            () -> new EvilBats(ArmorMaterials.IRON, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final DeferredItem<Item> EVIL_BATS_CHESTPLATE = ITEMS.register("evil_bats_chestplate",
            () -> new EvilBats(ArmorMaterials.IRON, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final DeferredItem<Item> EVIL_BATS_LEGGINGS = ITEMS.register("evil_bats_leggings",
            () -> new EvilBats(ArmorMaterials.IRON, ArmorItem.Type.LEGGINGS, new Item.Properties()));

    // Marika
    public static final DeferredItem<Item> MARIKA_HELMET = ITEMS.register("marika_helmet",
            () -> new Marika(ArmorMaterials.GOLD, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final DeferredItem<Item> MARIKA_CHESTPLATE = ITEMS.register("marika_chestplate",
            () -> new Marika(ArmorMaterials.GOLD, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final DeferredItem<Item> MARIKA_LEGGINGS = ITEMS.register("marika_leggings",
            () -> new Marika(ArmorMaterials.GOLD, ArmorItem.Type.LEGGINGS, new Item.Properties()));

    // Napoleon Ghost
    public static final DeferredItem<Item> NAPOLEON_GHOST_HELMET = ITEMS.register("napoleon_ghost_helmet",
            () -> new NapoleonGhost(ArmorMaterials.IRON, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final DeferredItem<Item> NAPOLEON_GHOST_CHESTPLATE = ITEMS.register("napoleon_ghost_chestplate",
            () -> new NapoleonGhost(ArmorMaterials.IRON, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final DeferredItem<Item> NAPOLEON_GHOST_LEGGINGS = ITEMS.register("napoleon_ghost_leggings",
            () -> new NapoleonGhost(ArmorMaterials.IRON, ArmorItem.Type.LEGGINGS, new Item.Properties()));

    // Nox Knight
    public static final DeferredItem<Item> NOX_KNIGHT_HELMET = ITEMS.register("nox_knight_helmet",
            () -> new NoxKnight(ArmorMaterials.NETHERITE, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final DeferredItem<Item> NOX_KNIGHT_CHESTPLATE = ITEMS.register("nox_knight_chestplate",
            () -> new NoxKnight(ArmorMaterials.NETHERITE, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final DeferredItem<Item> NOX_KNIGHT_LEGGINGS = ITEMS.register("nox_knight_leggings",
            () -> new NoxKnight(ArmorMaterials.NETHERITE, ArmorItem.Type.LEGGINGS, new Item.Properties()));

    // Quinbee
    public static final DeferredItem<Item> QUINBEE_HELMET = ITEMS.register("quinbee_helmet",
            () -> new Quinbee(ArmorMaterials.GOLD, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final DeferredItem<Item> QUINBEE_CHESTPLATE = ITEMS.register("quinbee_chestplate",
            () -> new Quinbee(ArmorMaterials.GOLD, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final DeferredItem<Item> QUINBEE_LEGGINGS = ITEMS.register("quinbee_leggings",
            () -> new Quinbee(ArmorMaterials.GOLD, ArmorItem.Type.LEGGINGS, new Item.Properties()));

    // Rider Barons
    public static final DeferredItem<Item> RIDER_BARONS_HELMET = ITEMS.register("riderbarons_helmet",
            () -> new RiderBarons(ArmorMaterials.DIAMOND, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final DeferredItem<Item> RIDER_BARONS_CHESTPLATE = ITEMS.register("riderbarons_chestplate",
            () -> new RiderBarons(ArmorMaterials.DIAMOND, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final DeferredItem<Item> RIDER_BARONS_LEGGINGS = ITEMS.register("riderbarons_leggings",
            () -> new RiderBarons(ArmorMaterials.DIAMOND, ArmorItem.Type.LEGGINGS, new Item.Properties()));

    // Rider Necrom
    public static final DeferredItem<Item> RIDER_NECROM_HELMET = ITEMS.register("rider_necrom_helmet",
            () -> new RiderNecrom(ArmorMaterials.IRON, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final DeferredItem<Item> RIDER_NECROM_CHESTPLATE = ITEMS.register("rider_necrom_chestplate",
            () -> new RiderNecrom(ArmorMaterials.IRON, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final DeferredItem<Item> RIDER_NECROM_LEGGINGS = ITEMS.register("rider_necrom_leggings",
            () -> new RiderNecrom(ArmorMaterials.IRON, ArmorItem.Type.LEGGINGS, new Item.Properties()));

    // Sigurd
    public static final DeferredItem<Item> SIGURD_HELMET = ITEMS.register("sigurd_helmet",
            () -> new Sigurd(ArmorMaterials.IRON, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final DeferredItem<Item> SIGURD_CHESTPLATE = ITEMS.register("sigurd_chestplate",
            () -> new Sigurd(ArmorMaterials.IRON, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final DeferredItem<Item> SIGURD_LEGGINGS = ITEMS.register("sigurd_leggings",
            () -> new Sigurd(ArmorMaterials.IRON, ArmorItem.Type.LEGGINGS, new Item.Properties()));

    // Tyrant
    public static final DeferredItem<Item> TYRANT_HELMET = ITEMS.register("tyrant_helmet",
            () -> new Tyrant(ArmorMaterials.DIAMOND, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final DeferredItem<Item> TYRANT_CHESTPLATE = ITEMS.register("tyrant_chestplate",
            () -> new Tyrant(ArmorMaterials.DIAMOND, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final DeferredItem<Item> TYRANT_LEGGINGS = ITEMS.register("tyrant_leggings",
            () -> new Tyrant(ArmorMaterials.DIAMOND, ArmorItem.Type.LEGGINGS, new Item.Properties()));

    // Zangetsu Shin
    public static final DeferredItem<Item> ZANGETSU_SHIN_HELMET = ITEMS.register("zangetsu_shin_helmet",
            () -> new zangetsuShin(ArmorMaterials.DIAMOND, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final DeferredItem<Item> ZANGETSU_SHIN_CHESTPLATE = ITEMS.register("zangetsu_shin_chestplate",
            () -> new zangetsuShin(ArmorMaterials.DIAMOND, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final DeferredItem<Item> ZANGETSU_SHIN_LEGGINGS = ITEMS.register("zangetsu_shin_leggings",
            () -> new zangetsuShin(ArmorMaterials.DIAMOND, ArmorItem.Type.LEGGINGS, new Item.Properties()));

}