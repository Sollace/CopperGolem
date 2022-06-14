package com.sollace.coppergolem.mixin;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.sollace.coppergolem.entity.LightningAttractionUtil;

import java.util.Optional;

@Mixin(ServerWorld.class)
public class MixinServerWorld {
    @Inject(method = "getLightningRodPos", at = @At("HEAD"), cancellable = true)
    private void getLightningRodPos(BlockPos pos, CallbackInfoReturnable<Optional<BlockPos>> info) {
        ServerWorld self = (ServerWorld)(Object)this;
        Optional<BlockPos> p = LightningAttractionUtil.getGolemAtPos(self, pos);
        if (p.isPresent()) {
            info.setReturnValue(p);
        }
    }
}
