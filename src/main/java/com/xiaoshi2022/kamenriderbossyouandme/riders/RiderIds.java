package com.xiaoshi2022.kamenriderbossyouandme.riders;

import net.minecraft.resources.ResourceLocation;

import static com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME.MODID;

public class RiderIds {
    public static final ResourceLocation BRAIN_ID = fromString("brain");
    
    public static ResourceLocation fromString(String id) {
        return ResourceLocation.fromNamespaceAndPath(MODID, id);
    }
}