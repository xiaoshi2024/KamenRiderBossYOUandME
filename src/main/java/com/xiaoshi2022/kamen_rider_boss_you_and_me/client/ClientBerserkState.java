package com.xiaoshi2022.kamen_rider_boss_you_and_me.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientBerserkState {
    private static boolean isBerserk = false;
    
    public static void setBerserk(boolean berserk) {
        isBerserk = berserk;
    }
    
    public static boolean isBerserk() {
        return isBerserk;
    }
}