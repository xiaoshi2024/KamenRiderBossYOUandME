package com.xiaoshi2022.kamen_rider_boss_you_and_me.villagers;

import com.google.common.collect.ImmutableSet;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBlocks;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class TimeJackerProfession {
    /* ----------------  POI  ---------------- */
    public static final DeferredRegister<PoiType> POI_TYPE = 
            DeferredRegister.create(ForgeRegistries.POI_TYPES, kamen_rider_boss_you_and_me.MODID);

    public static final RegistryObject<PoiType> TIME_JACKER_POI_TYPE = 
            POI_TYPE.register("time_jacker_poi_type", () -> new PoiType(
                    ImmutableSet.copyOf(ModBlocks.TIME_JACKER_TABLE_BLOCK.get()
                            .getStateDefinition()
                            .getPossibleStates()),
                    1,   // maxTickets
                    1    // validRange
            ));

    /* -------------- Profession ------------- */
    public static final DeferredRegister<VillagerProfession> PROFESSION = 
            DeferredRegister.create(ForgeRegistries.VILLAGER_PROFESSIONS, kamen_rider_boss_you_and_me.MODID);

    public static final RegistryObject<VillagerProfession> TIME_JACKER_PROFESSION = 
            PROFESSION.register("time_jacker_profession",
                    () -> new VillagerProfession(
                            "time_jacker_profession",
                            holder -> holder.get() == TIME_JACKER_POI_TYPE.get(),
                            holder -> holder.get() == TIME_JACKER_POI_TYPE.get(),
                            ImmutableSet.of(ModItems.AIZIOWC.get()),   // 可持有物品
                            ImmutableSet.of(Block.byItem(ModItems.AIZIOWC.get())),
                            SoundEvents.VILLAGER_WORK_LIBRARIAN          // 工作音效
                    ));
}