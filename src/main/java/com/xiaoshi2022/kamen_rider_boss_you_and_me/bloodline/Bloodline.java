package com.xiaoshi2022.kamen_rider_boss_you_and_me.bloodline;

import net.minecraft.nbt.CompoundTag;

public class Bloodline {
    private float fangPurity;   // 0~1

    public float getFangPurity() {
        return fangPurity;
    }

    public void setFangPurity(float fangPurity) {
        this.fangPurity = Math.max(0F, Math.min(1F, fangPurity));
    }

    public void save(CompoundTag tag) {
        tag.putFloat("FangPurity", fangPurity);
    }

    public void load(CompoundTag tag) {
        fangPurity = tag.getFloat("FangPurity");
    }
}