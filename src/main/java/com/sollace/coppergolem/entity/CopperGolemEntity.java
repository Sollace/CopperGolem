package com.sollace.coppergolem.entity;

import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.Oxidizable;
import net.minecraft.block.Oxidizable.OxidizationLevel;
import net.minecraft.block.WallMountedBlock;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.block.BlockStatePredicate;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.function.MaterialPredicate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

import org.jetbrains.annotations.Nullable;

import com.sollace.coppergolem.GBlocks;
import com.sollace.coppergolem.GItems;
import com.sollace.coppergolem.GSounds;
import com.sollace.coppergolem.entity.ai.BlockInteraction;
import com.sollace.coppergolem.entity.ai.PressButtonGoal;
import com.sollace.coppergolem.entity.ai.VariantSpeedEscapeDangerGoal;
import com.sollace.coppergolem.entity.ai.VariantSpeedWanderAroundFarGoal;
import com.sollace.coppergolem.util.BlockStatePredicates;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class CopperGolemEntity extends GolemEntity {
    protected static final TrackedData<Integer> OXIDATION = DataTracker.registerData(CopperGolemEntity.class, TrackedDataHandlerRegistry.INTEGER);
    protected static final TrackedData<Integer> WIGGLING_NOSE_TIME = DataTracker.registerData(CopperGolemEntity.class, TrackedDataHandlerRegistry.INTEGER);
    protected static final TrackedData<Integer> SPINNING_HEAD_TIME = DataTracker.registerData(CopperGolemEntity.class, TrackedDataHandlerRegistry.INTEGER);

    public static final byte SCRAPE_STATUS = 4;
    public static final byte WAX_ON_STATUS = 5;
    public static final byte WAX_OFF_STATUS = 6;

    private static final BlockPattern PATTERN = BlockPatternBuilder.start()
            .aisle("|", "#")
            .aisle("~", "o")
            .where('~', CachedBlockPosition.matchesBlockState(MaterialPredicate.create(Material.AIR)))
            .where('|', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.LIGHTNING_ROD)))
            .where('#', CachedBlockPosition.matchesBlockState(BlockStatePredicates.forTag(GBlocks.Tags.COPPER_GOLEM_MATERIALS)))
            .where('o', CachedBlockPosition.matchesBlockState(BlockStatePredicates.forTag(GBlocks.Tags.COPPER_BUTTONS)))
            .build();

    protected boolean waxed;

    private final Map<Identifier, BlockInteraction> finders = new HashMap<>();

    CopperGolemEntity(EntityType<CopperGolemEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected void initGoals() {
        goalSelector.add(1, new MeleeAttackGoal(this, 1, true));
        goalSelector.add(1, new VariantSpeedEscapeDangerGoal(this));
        goalSelector.add(2, new VariantSpeedWanderAroundFarGoal(this));
        goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 6));
        goalSelector.add(8, new LookAroundGoal(this));
        targetSelector.add(1, new PressButtonGoal(this, 14, 30));
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(OXIDATION, 0);
        dataTracker.startTracking(WIGGLING_NOSE_TIME, 0);
        dataTracker.startTracking(SPINNING_HEAD_TIME, 0);
    }

    public BlockInteraction getFinder(int maxDistance) {
        return finders.computeIfAbsent(Registry.ITEM.getId(getStackInHand(Hand.MAIN_HAND).getItem()), id -> {
            return BlockInteraction.create(this, maxDistance);
        });
    }

    @Override
    public boolean canPickupItem(ItemStack stack) {
        ItemStack current = this.getStackInHand(Hand.MAIN_HAND);

        if (!current.isEmpty() && ItemStack.canCombine(current, stack)) {
            return true;
        }

        return GItems.Tags.COPPER_GOLEM_CAN_PICK_UP.contains(stack.getItem());
    }

    @Override
    protected void loot(ItemEntity item) {

        ItemStack stack = item.getStack();

        if (canPickupItem(stack)) {
            ItemStack current = getEquippedStack(EquipmentSlot.MAINHAND);

            int canTake = current.getMaxCount() - current.getCount();

            if (canTake > 0) {
                if (canTake >= stack.getCount()) {
                    triggerItemPickedUpByEntityCriteria(item);
                    equipStack(EquipmentSlot.MAINHAND, stack);
                    handDropChances[EquipmentSlot.MAINHAND.getEntitySlotId()] = 2;
                    sendPickup(item, stack.getCount());
                    item.discard();
                } else {
                    item.setStack(stack.split(stack.getCount() - canTake));
                    current.increment(canTake);
                    equipStack(EquipmentSlot.MAINHAND, stack);
                    handDropChances[EquipmentSlot.MAINHAND.getEntitySlotId()] = 2;
                }
            }
        }
    }

    public void setOxidation(int oxidation) {
        dataTracker.set(OXIDATION, MathHelper.clamp(oxidation, 0, (Oxidizable.OxidizationLevel.values().length - 1) * 100));
    }

    public int getOxidation() {
        return dataTracker.get(OXIDATION);
    }

    public OxidizationLevel getDegradationLevel() {
        int oxidation = (int)Math.floor((float)getOxidation() / 100F);

        var levels = Oxidizable.OxidizationLevel.values();

        return levels[MathHelper.clamp(oxidation, 0, levels.length - 1)];
    }

    public void setDegradationLevel(OxidizationLevel level) {
        setOxidation(level.ordinal() * 100);
    }

    public boolean isWigglingNose() {
        return dataTracker.get(WIGGLING_NOSE_TIME) > 0;
    }

    public void wiggleNose() {
        dataTracker.set(WIGGLING_NOSE_TIME, 10);
    }

    public void expressDissappointment() {
        wiggleNose();
        playSound(SoundEvents.ENTITY_VILLAGER_NO, getSoundVolume(), getSoundPitch());
    }

    public int getHeadSpinTime() {
        return dataTracker.get(SPINNING_HEAD_TIME);
    }

    public void spinHead() {
        dataTracker.set(SPINNING_HEAD_TIME, 10);
    }

    @Override
    public float getMovementSpeed() {
        return 0.6F / (1 + getDegradationLevel().ordinal());
    }

    @Override
    protected int getNextAirUnderwater(int air) {
        return air;
    }

    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || getDegradationLevel() == OxidizationLevel.OXIDIZED;
    }

    @Override
    public boolean isCollidable() {
        return inanimate;
    }

    public void tick() {
        inanimate = isImmobile();
        if (inanimate) {
            float pitch = this.getPitch();
            float bodyYaw = this.bodyYaw;
            float stepBobbingAmount = this.stepBobbingAmount;

            float limbAngle = this.limbAngle;
            float limbDistance = this.limbDistance;

            float handSwingProgress = this.handSwingProgress;

            super.tick();

            this.setPitch(pitch);
            this.prevPitch = pitch;
            this.bodyYaw = bodyYaw;
            this.prevBodyYaw = bodyYaw;
            this.prevStepBobbingAmount = stepBobbingAmount;

            this.limbAngle = limbAngle;
            this.limbDistance = limbDistance;
            this.lastLimbDistance = limbDistance;

            this.handSwingProgress = handSwingProgress;
            this.lastHandSwingProgress = handSwingProgress;
        } else {
            super.tick();
        }
    }

    @Override
    public void tickMovement() {
        setCanPickUpLoot(true);
        super.tickMovement();
        tickHandSwing();

        if (!waxed && random.nextFloat() < 0.05688889F) {
            setOxidation(getOxidation() + 1);
        }

        if (!inanimate) {
            if (isWigglingNose()) {
                dataTracker.set(WIGGLING_NOSE_TIME, dataTracker.get(WIGGLING_NOSE_TIME) - 1);
            }

            int headSpinTime = getHeadSpinTime();

            if (headSpinTime > 0) {
                if (headSpinTime == 10) {
                    playSound(GSounds.ENTITY_COPPER_GOLEM_WHIRL, getSoundVolume(), getSoundPitch());
                }
                dataTracker.set(SPINNING_HEAD_TIME, headSpinTime - 1);
            }

            if (getRandom().nextInt(1200) == 0) {
                wiggleNose();
            }

            if (getNavigation().isIdle() && getRandom().nextInt(1600) == 0) {
                spinHead();
            }
        }
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (stack.isIn(FabricToolTags.AXES)) {
            if (waxed) {
                waxed = false;
                stack.damage(1, player, t -> t.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
                world.sendEntityStatus(this, WAX_OFF_STATUS);
                playSound(SoundEvents.ITEM_AXE_WAX_OFF, 1, 1);

                return ActionResult.SUCCESS;
            } else if (getOxidation() >= 100) {
                setOxidation(100 * (getDegradationLevel().ordinal() - 1));
                stack.damage(1, player, t -> t.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
                world.sendEntityStatus(this, SCRAPE_STATUS);
                playSound(SoundEvents.ITEM_AXE_SCRAPE, 1, 1);
                spinHead();

                return ActionResult.SUCCESS;
            }
        } else if (stack.isOf(Items.HONEYCOMB) && !waxed) {
            waxed = true;
            world.sendEntityStatus(this, WAX_ON_STATUS);
            playSound(SoundEvents.ITEM_HONEYCOMB_WAX_ON, 1, 1);

            if (!player.getAbilities().creativeMode) {
                stack.decrement(1);
            }

            return ActionResult.SUCCESS;
        } else {
            ItemStack heldStack = getStackInHand(Hand.MAIN_HAND);
            if (heldStack.isEmpty() || ItemStack.canCombine(heldStack, stack)) {
                ItemStack newStack = stack.split(1);
                newStack.increment(heldStack.getCount());
                equipStack(EquipmentSlot.MAINHAND, stack);
                handDropChances[EquipmentSlot.MAINHAND.getEntitySlotId()] = 2;

                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }

    @Override
    public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
        if (getOxidation() >= 100) {
            setOxidation(100 * (getDegradationLevel().ordinal() - 1));
            world.sendEntityStatus(this, SCRAPE_STATUS);
            spinHead();
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
       super.writeCustomDataToNbt(nbt);
       nbt.putInt("oxidation", getOxidation());
       nbt.putBoolean("waxed", waxed);

       NbtCompound modules = new NbtCompound();
       finders.forEach((block, module) -> {
           modules.put(block.toString(), module.toNbt());
       });
       nbt.put("interactionMemories", modules);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
       super.readCustomDataFromNbt(nbt);
       setOxidation(nbt.getInt("oxidation"));
       waxed = nbt.getBoolean("waxed");

       finders.clear();
       NbtCompound modules = nbt.getCompound("interactionMemories");
       modules.getKeys().forEach(block -> {
          Identifier id = Identifier.tryParse(block);
          if (id != null) {
              finders.put(id, BlockInteraction.fromNbt(this, modules.getCompound(block)));
          }
       });
    }

    @Override
    public void handleStatus(byte status) {
        if (status == SCRAPE_STATUS) {
            produceParticles(ParticleTypes.SCRAPE);
        } else if (status == WAX_ON_STATUS) {
            produceParticles(ParticleTypes.WAX_ON);
        } else if (status == WAX_OFF_STATUS) {
            produceParticles(ParticleTypes.WAX_OFF);
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
        playSound(GSounds.ENTITY_COPPER_GOLEM_STEP, 1, 1);
    }

    @Nullable
    protected SoundEvent getAmbientSound() {
       return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return GSounds.ENTITY_COPPER_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return GSounds.ENTITY_COPPER_GOLEM_DEATH;
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

        OxidizationLevel[] oxidation = new OxidizationLevel[] { OxidizationLevel.UNAFFECTED };

        iterateAround(result, position -> {
            if (position.getBlockState().getBlock() instanceof WallMountedBlock) {
                world.setBlockState(position.getBlockPos(), Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
                world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, position.getBlockPos(), Block.getRawIdFromState(position.getBlockState()));
            }

            if (position.getBlockState().isIn(GBlocks.Tags.COPPER_GOLEM_MATERIALS) && position.getBlockState().getBlock() instanceof Oxidizable) {
                oxidation[0] = ((Oxidizable)position.getBlockState().getBlock()).getDegradationLevel();
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
        golem.setDegradationLevel(oxidation[0]);

        world.spawnEntity(golem);

        if (oxidation[0] != OxidizationLevel.OXIDIZED || golem.getRandom().nextBoolean()) {
            golem.spinHead();
        }

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
