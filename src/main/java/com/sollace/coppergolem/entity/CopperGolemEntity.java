package com.sollace.coppergolem.entity;

import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.Oxidizable;
import net.minecraft.block.Oxidizable.OxidizationLevel;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.WanderAroundPointOfInterestGoal;
import net.minecraft.entity.ai.goal.WanderNearTargetGoal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.block.BlockStatePredicate;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.function.MaterialPredicate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

import com.sollace.coppergolem.CopperButtonBlock;
import com.sollace.coppergolem.GBlocks;

import java.util.function.Consumer;

public class CopperGolemEntity extends GolemEntity {

    protected static final TrackedData<Integer> OXIDATION = DataTracker.registerData(CopperGolemEntity.class, TrackedDataHandlerRegistry.INTEGER);

    public static final byte SCRAPE_STATUS = 4;

    private static final BlockPattern PATTERN = BlockPatternBuilder.start()
            .aisle("|", "#")
            .aisle("~", "o")
            .where('~', CachedBlockPosition.matchesBlockState(MaterialPredicate.create(Material.AIR)))
            .where('|', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.LIGHTNING_ROD)))
            .where('#', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.COPPER_BLOCK)))
            .where('o', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(GBlocks.COPPER_BUTTON)))
            .build();

    CopperGolemEntity(EntityType<CopperGolemEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected void initGoals() {
        goalSelector.add(1, new MeleeAttackGoal(this, 1, true));
        goalSelector.add(1, new EscapeDangerGoal(this, 1));
        goalSelector.add(2, new WanderNearTargetGoal(this, 0.9, 32));
        goalSelector.add(2, new WanderAroundPointOfInterestGoal(this, 0.6, false));
        goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 6));
        goalSelector.add(8, new LookAroundGoal(this));
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(OXIDATION, 0);
    }

    public void setOxidation(int oxidation) {
        dataTracker.set(OXIDATION, Math.max(0, oxidation));
    }

    public int getOxidation() {
        return dataTracker.get(OXIDATION);
    }

    public OxidizationLevel getDegradationLevel() {
        int oxidation = (int)Math.floor((float)getOxidation() / 100F);

        var levels = Oxidizable.OxidizationLevel.values();

        return levels[Math.abs(oxidation) % levels.length];
    }

    public void setDegradationLevel(OxidizationLevel level) {
        setOxidation(level.ordinal() * 100);
    }

    @Override
    public float getMovementSpeed() {
        return super.getMovementSpeed() / (1 + getDegradationLevel().ordinal());
    }

    @Override
    protected int getNextAirUnderwater(int air) {
        return air;
    }

    @Override
    public void tickMovement() {
        super.tickMovement();

        if (random.nextFloat() < 0.05688889F) {
            setOxidation(getOxidation() + 1);
        }
    }

    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (stack.isIn(FabricToolTags.AXES) && getOxidation() >= 100) {
            setOxidation(100 * (getDegradationLevel().ordinal() - 1));
            stack.damage(1, player, t -> t.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
            world.sendEntityStatus(this, SCRAPE_STATUS);

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
       super.writeCustomDataToNbt(nbt);
       nbt.putInt("oxidation", getOxidation());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
       super.readCustomDataFromNbt(nbt);
       setOxidation(nbt.getInt("oxidation"));
    }

    @Override
    public void handleStatus(byte status) {
        if (status == SCRAPE_STATUS) {
            playSound(SoundEvents.ITEM_AXE_SCRAPE, 1, 1);
            produceParticles(ParticleTypes.SCRAPE);
        } else {
           super.handleStatus(status);
        }
    }

    protected void produceParticles(ParticleEffect parameters) {
        for (int i = 0; i < 5; ++i) {
           world.addParticle(parameters,
                   getParticleX(1), getRandomBodyY() + 1, getParticleZ(1),
                   random.nextGaussian() * 0.02,
                   random.nextGaussian() * 0.02,
                   random.nextGaussian() * 0.02
           );
        }
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.BLOCK_COPPER_STEP, 1, 1);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_IRON_GOLEM_DEATH;
    }

    @Override
    public float getSoundPitch() {
        return 1.8F;
    }

    public static boolean tryBuild(World world, BlockPos pos) {
        BlockPattern.Result result = PATTERN.searchAround(world, pos);
        if (result == null) {
            return false;
        }

        iterateAround(result, position -> {
            if (position.getBlockState().getBlock() instanceof CopperButtonBlock) {
                world.setBlockState(position.getBlockPos(), Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
                world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, position.getBlockPos(), Block.getRawIdFromState(position.getBlockState()));
            }
        });

        iterateAround(result, position -> {
            world.setBlockState(position.getBlockPos(), Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
            world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, position.getBlockPos(), Block.getRawIdFromState(position.getBlockState()));
        });

        CopperGolemEntity golem = GEntities.COPPER_GOLEM.create(world);
        BlockPos center = result.translate(0, 1, 0).getBlockPos();
        golem.refreshPositionAndAngles(center.getX() + 0.5, center.getY() + 0.1, center.getZ() + 0.5, result.getForwards().asRotation(), 0);
        golem.bodyYaw = result.getForwards().asRotation();
        golem.headYaw = result.getForwards().asRotation();

        world.spawnEntity(golem);

        iterateAround(result, position -> {
            world.updateNeighbors(position.getBlockPos(), Blocks.AIR);
        });

        return true;
    }

    private static void iterateAround(BlockPattern.Result result, Consumer<CachedBlockPosition> action) {
        for (int x = 0; x < result.getWidth(); x++) {
            for (int y = 0; y < result.getHeight(); y++) {
                for (int z = 0; z < result.getDepth(); z++) {
                    action.accept(result.translate(x, y, z));
                }
            }
        }
    }

}