package com.sollace.coppergolem.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.argument.EntityAnchorArgumentType.EntityAnchor;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.WorldEvents;

import com.sollace.coppergolem.entity.CopperGolemEntity;

import java.util.Optional;

public class MineBlockGoal extends Goal {

    private final CopperGolemEntity mob;

    private Optional<MiningInformation> miningInfo = Optional.empty();

    public MineBlockGoal(CopperGolemEntity mob) {
        this.mob = mob;
    }

    public void startMining(BlockState state, BlockPos pos) {
        miningInfo = Optional.of(new MiningInformation(state, pos));
    }

    @Override
    public boolean canStart() {
        return miningInfo.filter(MiningInformation::isStillValid).isPresent();
    }

    @Override
    public boolean shouldContinue() {
        return miningInfo.filter(MiningInformation::isStillValid).isPresent();
    }

    public void start() {

    }

    public void stop() {
        miningInfo = Optional.empty();
    }

    public void tick() {
        miningInfo = miningInfo.filter(MiningInformation::isStillValid).filter(MiningInformation::tick);
    }

    private class MiningInformation {
        private final BlockState state;
        private final BlockPos pos;
        protected float progress;

        public MiningInformation(BlockState state, BlockPos pos) {
            this.state = state;
            this.pos = pos;
        }

        public boolean isStillValid() {
            return mob.world.getBlockState(pos) == state;
        }

        public boolean tick() {
            float hardness = state.getHardness(mob.world, pos);
            if (hardness < 0) {
                progress++;
                return progress < 120;
            }

            ItemStack item = mob.getMainHandStack();
            boolean canMine = !state.isToolRequired() || item.isSuitableFor(state);

            float breakingDelta = mob.getMiningSpeed(state) / hardness / (canMine ? 30F : 100F);

            mob.getNavigation().stop();

            progress += breakingDelta;
            mob.swingHand(Hand.MAIN_HAND);
            mob.lookAt(EntityAnchor.FEET, new Vec3d(pos.getX(), pos.getY(), pos.getZ()));
            mob.world.setBlockBreakingInfo(mob.getId(), pos, (int)(progress * 10));

            if ((int)(progress * 100) % 2 == 0) {
                BlockSoundGroup group = state.getSoundGroup();
                mob.world.playSound(null, pos,
                        group.getHitSound(),
                        SoundCategory.BLOCKS,
                        (group.getVolume() + 1) / 8F,
                        group.getPitch() / 2F
                );
            }

            if (progress > 1) {
                Block block = state.getBlock();

                if (mob.world.getGameRules().get(GameRules.DO_MOB_GRIEFING).get()) {
                    PlayerEntity nearestPlayer = mob.world.getClosestPlayer(mob, 10);
                    mob.world.syncWorldEvent(null, WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(state));
                    if (nearestPlayer == null) {
                        mob.world.breakBlock(pos, canMine, mob);
                    } else {
                        BlockEntity be = mob.world.getBlockEntity(pos);
                        block.onBreak(mob.world, pos, state, nearestPlayer);
                        boolean removed = mob.world.removeBlock(pos, false);
                        if (removed) {
                            block.onBroken(mob.world, pos, state);
                        }

                        ItemStack heldItem = mob.getMainHandStack();
                        ItemStack copy = heldItem.copy();
                        heldItem.getItem().postMine(heldItem, mob.world, state, pos, mob);
                        if (removed && canMine) {
                            block.afterBreak(mob.world, nearestPlayer, pos, state, be, copy);
                        }
                    }
                }
                mob.spinHead();
                return false;
            }

            return true;
        }
    }
}
