package com.xiaoshi2022.kamen_rider_boss_you_and_me.client;

import net.minecraft.client.player.Input;

public class BerserkMovementInput extends Input {
    
    private float targetForwardImpulse;
    private float targetLeftImpulse;
    private boolean targetJumping;
    
    public BerserkMovementInput() {
        this.targetForwardImpulse = 0.0F;
        this.targetLeftImpulse = 0.0F;
        this.targetJumping = false;
    }
    
    public void setMovement(float forwardImpulse, float leftImpulse, boolean jumping) {
        this.targetForwardImpulse = forwardImpulse;
        this.targetLeftImpulse = leftImpulse;
        this.targetJumping = jumping;
    }
    
    @Override
    public void tick(boolean slowDown, float f) {
        this.forwardImpulse = Math.abs(targetForwardImpulse);
        this.leftImpulse = Math.abs(targetLeftImpulse);
        this.jumping = targetJumping;
        
        this.up = targetForwardImpulse > 0;
        this.down = targetForwardImpulse < 0;
        this.left = targetLeftImpulse > 0;
        this.right = targetLeftImpulse < 0;
        this.shiftKeyDown = false;
    }
}
