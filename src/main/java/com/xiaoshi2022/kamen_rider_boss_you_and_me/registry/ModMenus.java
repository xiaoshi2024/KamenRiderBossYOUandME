package com.xiaoshi2022.kamen_rider_boss_you_and_me.registry;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.curio.EntityCuriosListMenu;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = 
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, kamen_rider_boss_you_and_me.MODID);
    
    public static final RegistryObject<MenuType<EntityCuriosListMenu>> ENTITY_CURIOS_LIST = 
            MENUS.register("entity_curios_list", 
                    () -> EntityCuriosListMenu.TYPE);
}