package com.xiaoshi2022.kamen_rider_boss_you_and_me.registry;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.StoriousMonsterBook;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.food.InvesMeat;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.giifusteamp;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.Necrom_eye;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.aiziowc;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.bananafruit;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Mega_uiorder;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_necrom.RidernecromItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, kamen_rider_boss_you_and_me.MODID);

    public static final RegistryObject<giifusteamp> GIIFUSTEAMP = ITEMS.register("giifusteamp",
            () -> new giifusteamp(new Item.Properties()));
    public static final RegistryObject<aiziowc> AIZIOWC = ITEMS.register("aiziowc",
            () -> new aiziowc(new Item.Properties()));
    public static final RegistryObject<StoriousMonsterBook> STORIOUSMONSTERBOOK = ITEMS.register("storiousmonsterbook",
            () -> new StoriousMonsterBook(new Item.Properties()));
    public static final RegistryObject<Necrom_eye> NECROM_EYE = ITEMS.register("necrom_eye",
            () -> new Necrom_eye(new Item.Properties()));
    public static final RegistryObject<bananafruit> BANANAFRUIT = ITEMS.register("bananafruit",
            () -> new bananafruit(new Item.Properties()));
    public static final RegistryObject<Mega_uiorder> MEGA_UIORDER_ITEM = ITEMS.register("mega_uiorder_item",
            () -> new Mega_uiorder(new Item.Properties()));
    public static final RegistryObject<Item> INVES_MEAT = ITEMS.register("inves_meat", InvesMeat::new);
    public static final RegistryObject<sengokudrivers_epmty> SENGOKUDRIVERS_EPMTY = ITEMS.register("sengokudrivers_epmty",
            () -> new sengokudrivers_epmty(new Item.Properties()));

    public static final RegistryObject<RidernecromItem> RIDERNECROM_HELMET = ITEMS.register("ridernecrom_helmet", () -> new RidernecromItem(ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<RidernecromItem> RIDERNECROM_CHESTPLATE = ITEMS.register("ridernecrom_chestplate", () -> new RidernecromItem(ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<RidernecromItem> RIDERNECROM_LEGGINGS = ITEMS.register("ridernecrom_leggings", () -> new RidernecromItem(ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<RidernecromItem> RIDERNECROM_BOOTS = ITEMS.register("ridernecrom_boots", () -> new RidernecromItem(ArmorItem.Type.BOOTS, new Item.Properties()));


}