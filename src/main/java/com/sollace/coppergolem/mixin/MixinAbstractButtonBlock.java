package com.sollace.coppergolem.mixin;

import net.minecraft.block.AbstractButtonBlock;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.sollace.coppergolem.CustomDurationButton;

@Mixin(AbstractButtonBlock.class)
public class MixinAbstractButtonBlock {
    @Inject(method = "getPressTicks", at = @At("HEAD"), cancellable = true)
    private void onGetPressTicks(CallbackInfoReturnable<Integer> info) {
        if (this instanceof CustomDurationButton) {
            info.setReturnValue(((CustomDurationButton)this).getCustomPressTicks());
        }
    }
}
