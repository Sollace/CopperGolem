package com.sollace.coppergolem.mixin;

import com.sollace.coppergolem.CustomDurationButton;
import net.minecraft.block.AbstractButtonBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractButtonBlock.class)
public class MixinAbstractButtonBlock {
    @Inject(method = "getPressTicks", at = @At("HEAD"), cancellable = true)
    private void onGetPressTicks(CallbackInfoReturnable<Integer> info) {
        if (this instanceof CustomDurationButton) {
            info.setReturnValue(((CustomDurationButton) this).getCustomPressTicks());
        }
    }
}
