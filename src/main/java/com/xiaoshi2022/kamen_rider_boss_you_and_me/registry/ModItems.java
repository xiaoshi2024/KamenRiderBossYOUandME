package com.xiaoshi2022.kamen_rider_boss_you_and_me.registry;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.StoriousMonsterBook;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.food.InvesMeat;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.giifusteamp;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.*;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.dragonfruit;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.weapon.Globalism;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Mega_uiorder;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.baron_lemons.baron_lemonItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.duke.Duke;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_barons.rider_baronsItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_necrom.RidernecromItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.zangetsu_shin.ZangetsuShinItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sigurd.Sigurd;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.marika.Marika;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.dark_orangels.Dark_orangels;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.tyrant.TyrantItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, kamen_rider_boss_you_and_me.MODID);

    // 武器道具
    // 假设你已经有 ModItems.ITEMS
    public static final RegistryObject<Globalism> GLOBALISM = ModItems.ITEMS.register(
            "globalism",
            () -> new Globalism(new Item.Properties())
    );

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


    public static final RegistryObject<giifusteamp> GIIFUSTEAMP = ITEMS.register("giifusteamp",
            () -> new giifusteamp(new Item.Properties()));
    public static final RegistryObject<aiziowc> AIZIOWC = ITEMS.register("aiziowc",
            () -> new aiziowc(new Item.Properties()));
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


    public static final RegistryObject<Mega_uiorder> MEGA_UIORDER_ITEM = ITEMS.register("mega_uiorder_item",
            () -> new Mega_uiorder(new Item.Properties()));
    public static final RegistryObject<Item> INVES_MEAT = ITEMS.register("inves_meat", InvesMeat::new);
    public static final RegistryObject<sengokudrivers_epmty> SENGOKUDRIVERS_EPMTY = ITEMS.register("sengokudrivers_epmty",
            () -> new sengokudrivers_epmty(new Item.Properties()));
    public static final RegistryObject<Genesis_driver> GENESIS_DRIVER = ITEMS.register("genesis_driver",
            () -> new Genesis_driver(new Item.Properties()));

    public static final RegistryObject<RidernecromItem> RIDERNECROM_HELMET = ITEMS.register("ridernecrom_helmet", () -> new RidernecromItem(ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<RidernecromItem> RIDERNECROM_CHESTPLATE = ITEMS.register("ridernecrom_chestplate", () -> new RidernecromItem(ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<RidernecromItem> RIDERNECROM_LEGGINGS = ITEMS.register("ridernecrom_leggings", () -> new RidernecromItem(ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<RidernecromItem> RIDERNECROM_BOOTS = ITEMS.register("ridernecrom_boots", () -> new RidernecromItem(ArmorItem.Type.BOOTS, new Item.Properties()));

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

    // Tyrant形态盔甲
    public static final RegistryObject<TyrantItem> TYRANT_HELMET = ITEMS.register("tyrant_helmet", () -> new TyrantItem(ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<TyrantItem> TYRANT_CHESTPLATE = ITEMS.register("tyrant_chestplate", () -> new TyrantItem(ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<TyrantItem> TYRANT_LEGGINGS = ITEMS.register("tyrant_leggings", () -> new TyrantItem(ArmorItem.Type.LEGGINGS, new Item.Properties()));

}