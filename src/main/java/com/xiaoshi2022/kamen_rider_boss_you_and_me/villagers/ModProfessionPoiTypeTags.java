package com.xiaoshi2022.kamen_rider_boss_you_and_me.villagers;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.PoiTypeTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.PoiTypeTags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModProfessionPoiTypeTags extends PoiTypeTagsProvider {
    public ModProfessionPoiTypeTags(PackOutput p_256012_, CompletableFuture<HolderLookup.Provider> p_256617_, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(p_256012_, p_256617_, kamen_rider_boss_you_and_me.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // 一次写全
        tag(PoiTypeTags.ACQUIRABLE_JOB_SITE)
                .addOptional(new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "time_jacker_poi_type"));
    }
}
