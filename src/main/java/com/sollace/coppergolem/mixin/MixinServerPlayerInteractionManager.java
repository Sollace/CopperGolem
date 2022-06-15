package com.sollace.coppergolem.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.sollace.coppergolem.entity.CopperGolemUtil;
import com.sollace.coppergolem.entity.ai.LearnedDuties;

@Mixin(ServerPlayerInteractionManager.class)
abstract class MixinServerPlayerInteractionManager {
    @Shadow
    protected ServerWorld world;
    @Shadow
    protected @Final ServerPlayerEntity player;

    private BlockState interactingState;

    @Inject(method = "tryBreakBlock", at = @At("HEAD"))
    private void beforeTryBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> info) {
        interactingState = world.getBlockState(pos);
    }

    @Inject(method = "tryBreakBlock", at = @At("RETURN"))
    private void onTryBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> info) {
        if (info.getReturnValueZ()) {
            ItemStack stack = player.getMainHandStack();
            CopperGolemUtil.broadCastLesson(world, interactingState, pos, stack, LearnedDuties.Duty.LEFT_CLICK);
        }
    }

    @Inject(method = "interactBlock", at = @At("HEAD"))
    private void beforeInteractBlock(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> info) {
        interactingState = world.getBlockState(hitResult.getBlockPos());
    }

    @Inject(method = "interactBlock", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onInteractBlock(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> info) {
        if (info.getReturnValue().isAccepted()) {
            CopperGolemUtil.broadCastLesson(world, interactingState, hitResult.getBlockPos(), stack, LearnedDuties.Duty.RIGHT_CLICK);
        }
    }
}
