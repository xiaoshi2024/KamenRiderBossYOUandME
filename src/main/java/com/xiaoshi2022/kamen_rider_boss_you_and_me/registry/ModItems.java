package com.xiaoshi2022.kamen_rider_boss_you_and_me.registry;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.*;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.food.InvesMeat;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.*;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.weapon.Globalism;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.weapon.TwoWeaponGunItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.weapon.TwoWeaponSwordItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.DarkRiderGhost.DarkRiderGhostItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.*;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.NapoleonGhost.NapoleonGhostItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.baron_lemons.baron_lemonItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.brain.Brain;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.darkKiva.DarkKivaItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.dark_orangels.Dark_orangels;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.duke.Duke;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.evilbats.EvilBatsArmor;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.marika.Marika;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_barons.rider_baronsItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_necrom.RidernecromItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sigurd.Sigurd;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.tyrant.TyrantItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.zangetsu_shin.ZangetsuShinItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, kamen_rider_boss_you_and_me.MODID);

    // 武器道具
    public static final RegistryObject<Globalism> GLOBALISM = ModItems.ITEMS.register(
            "globalism",
            () -> new Globalism(new Item.Properties())
    );

    //特殊道具
    public static final RegistryObject<BrainEyeglass> BRAIN_EYEGLASS = ModItems.ITEMS.register(
            "brain_eyeglass",
            ()-> new BrainEyeglass(new Item.Properties()));

    // 双面武器-剑形态
    public static final RegistryObject<TwoWeaponSwordItem> TWO_WEAPON_SWORD = ITEMS.register("two_weapon_sword",
            () -> {
                TwoWeaponSwordItem item = new TwoWeaponSwordItem(new Item.Properties());
                TwoWeaponSwordItem.ITEM = item;
                return item;
            });

    // 双面武器-枪形态
    public static final RegistryObject<TwoWeaponGunItem> TWO_WEAPON_GUN = ITEMS.register("two_weapon_gun",
            () -> {
                TwoWeaponGunItem item = new TwoWeaponGunItem(new Item.Properties());
                TwoWeaponGunItem.ITEM = item;
                return item;
            });

    public static final RegistryObject<Item> THRONE_BLOCK_ITEM = ITEMS.register("throne_block_item",
            () -> new BlockItem(ModBlocks.THRONE_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Item> GIIFU_SLEEPING_STATE_ITEM = ITEMS.register("giifu_sleeping_state_item",
            () -> new BlockItem(ModBlocks.GIIFU_SLEEPING_STATE_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Item> CURSE_BLOCK_ITEM = ITEMS.register("curse_block",
            () -> new BlockItem(ModBlocks.CURSE_BLOCK.get(), new Item.Properties()));

    // 时劫者工作台方块物品
    public static final RegistryObject<Item> TIME_JACKER_TABLE_ITEM = ITEMS.register("time_jacker_table",
            () -> new BlockItem(ModBlocks.TIME_JACKER_TABLE_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<BatStampItem> BAT_STAMP = ITEMS.register("bat_stamp",
            () -> new BatStampItem(new Item.Properties()));
            
    // 终止进程遥控器 - 可以强制解除创世纪驱动器变身的遥控器
    public static final RegistryObject<KillProcessItem> KILL_PROCESS = ITEMS.register("kill_process",
            () -> new KillProcessItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<TwoWeaponItem> TWO_WEAPON = ITEMS.register("two_weapon",
            () -> new TwoWeaponItem(new Item.Properties()));


    // 方块特效
    public static final RegistryObject<Item> BANANAS_BLOCK_ITEM = ITEMS.register("bananas_block",
            () -> new BlockItem(ModBlocks.BANANAS_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Item> LEMONX_ITEM = ITEMS.register("lemonx",
            () -> new BlockItem(ModBlocks.LEMON_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> MELON_ITEM = ITEMS.register("melonx",
            () -> new BlockItem(ModBlocks.MELON_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> CHERRYXS_ITEM = ITEMS.register("cherryxs",
            () -> new BlockItem(ModBlocks.CHERRYX_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Item> PEACHX_ITEM = ITEMS.register("peachx",
            () -> new BlockItem(ModBlocks.PEACHX_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Item> ORANGELSX_ITEM = ITEMS.register("orangelsx",
            () -> new BlockItem(ModBlocks.ORANGELSX_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Item> BLOODLINE_FANG = ITEMS.register("bloodline_fang",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<KivatBatTwoNdItem> KIVAT_BAT_TWO_ND_ITEM = ITEMS.register(
                        "kivat_bat_two_ndm",() -> new KivatBatTwoNdItem(new Item.Properties()));

    public static final RegistryObject<giifusteamp> GIIFUSTEAMP = ITEMS.register("giifusteamp",
            () -> new giifusteamp(new Item.Properties()));
    public static final RegistryObject<aiziowc> AIZIOWC = ITEMS.register("aiziowc",
            () -> new aiziowc(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<StoriousMonsterBook> STORIOUSMONSTERBOOK = ITEMS.register("storiousmonsterbook",
            () -> new StoriousMonsterBook(new Item.Properties()));
    public static final RegistryObject<Necrom_eye> NECROM_EYE = ITEMS.register("necrom_eye",
            () -> new Necrom_eye(new Item.Properties()));
    public static final RegistryObject<bananafruit> BANANAFRUIT = ITEMS.register("bananafruit",
            () -> new bananafruit(new Item.Properties()) {
                @Override
                public void onCraftedBy(ItemStack stack, Level world, Player player) {
                    stack.getOrCreateTag().putInt("is_lockseed", 1); // 标记为锁种
                }
            }
    );
    public static final RegistryObject<lemon_energy> LEMON_ENERGY = ITEMS.register("lemon_energy",
            () -> new lemon_energy(new Item.Properties()) {
                @Override
                public void onCraftedBy(ItemStack stack, Level world, Player player) {
                    stack.getOrCreateTag().putInt("is_lockseed", 1); // 标记为锁种
                }
            }
    );

    public static final RegistryObject<peach_energy> PEACH_ENERGY = ITEMS.register("peach_energy",
            () -> new peach_energy(new Item.Properties()) {
                @Override
                public void onCraftedBy(ItemStack stack, Level world, Player player) {
                    stack.getOrCreateTag().putInt("is_lockseed", 1); // 标记为锁种
                }
            }
    );
    public static final RegistryObject<orangefruit> ORANGEFRUIT = ITEMS.register("orangefruit",
            () -> new orangefruit(new Item.Properties()) {
                @Override
                public void onCraftedBy(ItemStack stack, Level world, Player player) {
                    stack.getOrCreateTag().putInt("is_lockseed", 1); // 标记为锁种
                }
            }
    );

    public static final RegistryObject<dragonfruit> DRAGONFRUIT = ITEMS.register("dragonfruit",
            () -> new dragonfruit(new Item.Properties()) {
                @Override
                public void onCraftedBy(ItemStack stack, Level world, Player player) {
                    stack.getOrCreateTag().putInt("is_lockseed", 1); // 标记为锁种
                }
            }
    );

    public static final RegistryObject<SakuraHurricaneLockseed> SAKURAHURRICANE_LOCKSEED = ITEMS.register("sakura_lockseed",
            () -> new SakuraHurricaneLockseed(new Item.Properties()) {
                @Override
                public void onCraftedBy(ItemStack stack, Level world, Player player) {
                    stack.getOrCreateTag().putInt("is_lockseed", 1); // 标记为锁种
                }
            }
    );

    public static final RegistryObject<GammaEyecon> GAMMA_EYECON = ITEMS.register("gamma_eyecon",
            () -> new GammaEyecon(new Item.Properties()));

    public static final RegistryObject<DarkRiderEyecon> DARK_RIDER_EYECON = ITEMS.register("dark_rider_eye",
            () -> new DarkRiderEyecon(new Item.Properties()));

    public static final RegistryObject<NapoleonEyecon> NAPOLEON_EYECON = ITEMS.register("nb_eye",
            () -> new NapoleonEyecon(new Item.Properties()));

    public static final RegistryObject<Mega_uiorder> MEGA_UIORDER_ITEM = ITEMS.register("mega_uiorder_item",
            () -> new Mega_uiorder(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> INVES_MEAT = ITEMS.register("inves_meat", InvesMeat::new);

    public static final RegistryObject<sengokudrivers_epmty> SENGOKUDRIVERS_EPMTY = ITEMS.register("sengokudrivers_epmty",
            () -> new sengokudrivers_epmty(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Genesis_driver> GENESIS_DRIVER = ITEMS.register("genesis_driver",
            () -> new Genesis_driver(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<DrakKivaBelt> DRAK_KIVA_BELT = ITEMS.register("drak_kiva_belt",
            () -> new DrakKivaBelt(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Two_sidriver> TWO_SIDRIVER = ITEMS.register("two_sidriver",
            () -> new Two_sidriver(new Item.Properties().stacksTo(1)));
    
    public static final RegistryObject<GhostDriver> GHOST_DRIVER = ITEMS.register("ghost_driver",
            () -> new GhostDriver(new Item.Properties().stacksTo(1)));
    
    public static final RegistryObject<BrainDriver> BRAIN_DRIVER = ITEMS.register("brain_driver",
            () -> new BrainDriver(new Item.Properties().stacksTo(1)));


    public static final RegistryObject<RidernecromItem> RIDERNECROM_HELMET = ITEMS.register("ridernecrom_helmet", () -> new RidernecromItem(ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<RidernecromItem> RIDERNECROM_CHESTPLATE = ITEMS.register("ridernecrom_chestplate", () -> new RidernecromItem(ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<RidernecromItem> RIDERNECROM_LEGGINGS = ITEMS.register("ridernecrom_leggings", () -> new RidernecromItem(ArmorItem.Type.LEGGINGS, new Item.Properties()));

    public static final RegistryObject<rider_baronsItem> RIDER_BARONS_HELMET = ITEMS.register("riderbarons_helmet", () -> new rider_baronsItem(ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<rider_baronsItem> RIDER_BARONS_CHESTPLATE = ITEMS.register("riderbarons_chestplate", () -> new rider_baronsItem(ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<rider_baronsItem> RIDER_BARONS_LEGGINGS = ITEMS.register("riderbarons_leggings", () -> new rider_baronsItem(ArmorItem.Type.LEGGINGS, new Item.Properties()));

    public static final RegistryObject<baron_lemonItem> BARON_LEMON_HELMET = ITEMS.register("baron_lemon_helmet", () -> new baron_lemonItem(ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<baron_lemonItem> BARON_LEMON_CHESTPLATE = ITEMS.register("baron_lemon_chestplate", () -> new baron_lemonItem(ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<baron_lemonItem> BARON_LEMON_LEGGINGS = ITEMS.register("baron_lemon_leggings", () -> new baron_lemonItem(ArmorItem.Type.LEGGINGS, new Item.Properties()));

    // 公爵形态盔甲
    public static final RegistryObject<Duke> DUKE_HELMET = ITEMS.register("duke_helmet", () -> new Duke(ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Duke> DUKE_CHESTPLATE = ITEMS.register("duke_chestplate", () -> new Duke(ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Duke> DUKE_LEGGINGS = ITEMS.register("duke_leggings", () -> new Duke(ArmorItem.Type.LEGGINGS, new Item.Properties()));

    public static final RegistryObject<ZangetsuShinItem> ZANGETSU_SHIN_HELMET = ITEMS.register("zangetsu_shin_helmet", () -> new ZangetsuShinItem(ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<ZangetsuShinItem> ZANGETSU_SHIN_CHESTPLATE = ITEMS.register("zangetsu_shin_chestplate", () -> new ZangetsuShinItem(ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<ZangetsuShinItem> ZANGETSU_SHIN_LEGGINGS = ITEMS.register("zangetsu_shin_leggings", () -> new ZangetsuShinItem(ArmorItem.Type.LEGGINGS, new Item.Properties()));

    public static final RegistryObject<Sigurd> SIGURD_HELMET = ITEMS.register("sigurd_helmet", () -> new Sigurd(ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Sigurd> SIGURD_CHESTPLATE = ITEMS.register("sigurd_chestplate", () -> new Sigurd(ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Sigurd> SIGURD_LEGGINGS = ITEMS.register("sigurd_leggings", () -> new Sigurd(ArmorItem.Type.LEGGINGS, new Item.Properties()));

    public static final RegistryObject<Marika> MARIKA_HELMET = ITEMS.register("marika_helmet", () -> new Marika(ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Marika> MARIKA_CHESTPLATE = ITEMS.register("marika_chestplate", () -> new Marika(ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Marika> MARIKA_LEGGINGS = ITEMS.register("marika_leggings", () -> new Marika(ArmorItem.Type.LEGGINGS, new Item.Properties()));

    // 黑暗橙天使形态盔甲
    public static final RegistryObject<Dark_orangels> DARK_ORANGELS_HELMET = ITEMS.register("dark_orangels_helmet", () -> new Dark_orangels(ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Dark_orangels> DARK_ORANGELS_CHESTPLATE = ITEMS.register("dark_orangels_chestplate", () -> new Dark_orangels(ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Dark_orangels> DARK_ORANGELS_LEGGINGS = ITEMS.register("dark_orangels_leggings", () -> new Dark_orangels(ArmorItem.Type.LEGGINGS, new Item.Properties()));

    // 艾比尔装甲
    public static final RegistryObject<EvilBatsArmor> EVIL_BATS_HELMET = ITEMS.register("evil_bats_helmet", () -> new EvilBatsArmor(ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<EvilBatsArmor> EVIL_BATS_CHESTPLATE = ITEMS.register("evil_bats_chestplate", () -> new EvilBatsArmor(ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<EvilBatsArmor> EVIL_BATS_LEGGINGS = ITEMS.register("evil_bats_leggings", () -> new EvilBatsArmor(ArmorItem.Type.LEGGINGS, new Item.Properties()));

    // Tyrant形态盔甲
    public static final RegistryObject<TyrantItem> TYRANT_HELMET = ITEMS.register("tyrant_helmet", () -> new TyrantItem(ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<TyrantItem> TYRANT_CHESTPLATE = ITEMS.register("tyrant_chestplate", () -> new TyrantItem(ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<TyrantItem> TYRANT_LEGGINGS = ITEMS.register("tyrant_leggings", () -> new TyrantItem(ArmorItem.Type.LEGGINGS, new Item.Properties()));

    public static final RegistryObject<DarkKivaItem> DARK_KIVA_HELMET = ITEMS.register("dark_kiva_helmet", () -> new DarkKivaItem(ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<DarkKivaItem> DARK_KIVA_CHESTPLATE = ITEMS.register("dark_kiva_chestplate", () -> new DarkKivaItem(ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<DarkKivaItem> DARK_KIVA_LEGGINGS = ITEMS.register("dark_kiva_leggings", () -> new DarkKivaItem(ArmorItem.Type.LEGGINGS, new Item.Properties()));

    public static final RegistryObject<DarkRiderGhostItem> DARK_RIDER_HELMET = ITEMS.register("dark_rider_ghost_helmet", () -> new DarkRiderGhostItem(ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<DarkRiderGhostItem> DARK_RIDER_CHESTPLATE = ITEMS.register("dark_rider_ghost_chestplate", () -> new DarkRiderGhostItem(ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<DarkRiderGhostItem> DARK_RIDER_LEGGINGS = ITEMS.register("dark_rider_ghost_leggings", () -> new DarkRiderGhostItem(ArmorItem.Type.LEGGINGS, new Item.Properties()));

    public static final RegistryObject<NapoleonGhostItem> NAPOLEON_GHOST_HELMET = ITEMS.register("napoleon_ghost_helmet", () -> new NapoleonGhostItem(ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<NapoleonGhostItem> NAPOLEON_GHOST_CHESTPLATE = ITEMS.register("napoleon_ghost_chestplate", () -> new NapoleonGhostItem(ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<NapoleonGhostItem> NAPOLEON_GHOST_LEGGINGS = ITEMS.register("napoleon_ghost_leggings", () -> new NapoleonGhostItem(ArmorItem.Type.LEGGINGS, new Item.Properties()));
    
    // Brain骑士装甲
    public static final RegistryObject<Brain> BRAIN_HELMET = ITEMS.register("brain_helmet", () -> new Brain(ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Brain> BRAIN_CHESTPLATE = ITEMS.register("brain_chestplate", () -> new Brain(ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Brain> BRAIN_LEGGINGS = ITEMS.register("brain_leggings", () -> new Brain(ArmorItem.Type.LEGGINGS, new Item.Properties()));

    // 基夫眼珠 - 击败基夫人形后掉落 (Geo物品)
    public static final RegistryObject<GiifuEyeBall> GIIFU_EYEBALL = ITEMS.register("giifu_eyeball",
            () -> new GiifuEyeBall(new Item.Properties()));

    // 刷怪蛋注册 - 修复了底色显示问题，使颜色更符合实体特性
    public static final RegistryObject<ForgeSpawnEggItem> LORD_BARON_SPAWN_EGG = ITEMS.register("lord_baron_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.LORD_BARON, 0x800080, 0x000000, new Item.Properties()));

    public static final RegistryObject<ForgeSpawnEggItem> KIVAT_BAT_TWO_ND_SPAWN_EGG = ITEMS.register("kivat_bat_two_nd_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.KIVAT_BAT_II, 0xFF0000, 0x000000, new Item.Properties()));

    public static final RegistryObject<ForgeSpawnEggItem> ELEMENTARY_INVES_HELHEIM_SPAWN_EGG = ITEMS.register("elementary_inves_helheim_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.INVES_HEILEHIM, 0xFF0000, 0x00FF00, new Item.Properties()));

    public static final RegistryObject<ForgeSpawnEggItem> GIIFU_HUMAN_SPAWN_EGG = ITEMS.register("giifu_human_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.GIIFU_HUMAN, 0xFF00FF, 0x000080, new Item.Properties()));

    public static final RegistryObject<ForgeSpawnEggItem> GIFFTARIAN_SPAWN_EGG = ITEMS.register("gifftarian_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.GIFFTARIAN, 0x00FF00, 0x808000, new Item.Properties()));

    public static final RegistryObject<ForgeSpawnEggItem> ANOTHER_ZI_O_SPAWN_EGG = ITEMS.register("another_zi_o_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.ANOTHER_ZI_O, 0xFF0000, 0xFFFFFF, new Item.Properties()));
    
    public static final RegistryObject<ForgeSpawnEggItem> ANOTHER_DEN_O_SPAWN_EGG = ITEMS.register("another_den_o_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.ANOTHER_DEN_O, 0xFF0000, 0x000000, new Item.Properties()));

    public static final RegistryObject<ForgeSpawnEggItem> GIIFU_DEMOS_SPAWN_EGG = ITEMS.register("giifu_demos_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.GIIFUDEMOS_ENTITY, 0x00FFFF, 0x800000, new Item.Properties()));

    public static final RegistryObject<ForgeSpawnEggItem> STORIOUS_SPAWN_EGG = ITEMS.register("storious_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.STORIOUS, 0xFF8000, 0x008080, new Item.Properties()));
            
    // 时劫者生成蛋
    public static final RegistryObject<ForgeSpawnEggItem> TIME_JACKER_SPAWN_EGG = ITEMS.register("time_jacker_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.TIME_JACKER, 0x0000FF, 0xFF0000, new Item.Properties()));

    // 眼魔实体生成蛋
    public static final RegistryObject<ForgeSpawnEggItem> GAMMA_S_SPAWN_EGG = ITEMS.register("gamma_s_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.GAMMA_S, 0x00FF00, 0x404040, new Item.Properties()));

    // 头脑机械变异体生成蛋
    public static final RegistryObject<ForgeSpawnEggItem> BRAIN_ROIDMUDE_SPAWN_EGG = ITEMS.register("brain_roidmude_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.BRAIN_ROIDMUDE, 0x4A0000, 0x800000, new Item.Properties()));

}