package com.sollace.coppergolem.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.Oxidizable;
import net.minecraft.block.Oxidizable.OxidationLevel;
import net.minecraft.block.WallMountedBlock;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.block.BlockStatePredicate;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.function.MaterialPredicate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.registry.Registries;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

import org.jetbrains.annotations.Nullable;

import com.sollace.coppergolem.GBlocks;
import com.sollace.coppergolem.GItems;
import com.sollace.coppergolem.GSounds;
import com.sollace.coppergolem.entity.ai.BlockInteraction;
import com.sollace.coppergolem.entity.ai.ChaseTargetGoal;
import com.sollace.coppergolem.entity.ai.LearnedDuties;
import com.sollace.coppergolem.entity.ai.MineBlockGoal;
import com.sollace.coppergolem.entity.ai.PressButtonGoal;
import com.sollace.coppergolem.entity.ai.VariantSpeedEscapeDangerGoal;
import com.sollace.coppergolem.entity.ai.VariantSpeedWanderAroundFarGoal;
import com.sollace.coppergolem.util.BlockStatePredicates;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class CopperGolemEntity extends GolemEntity {
    protected static final TrackedData<Integer> OXIDATION = DataTracker.registerData(CopperGolemEntity.class, TrackedDataHandlerRegistry.INTEGER);
    protected static final TrackedData<Integer> WIGGLING_NOSE_TIME = DataTracker.registerData(CopperGolemEntity.class, TrackedDataHandlerRegistry.INTEGER);
    protected static final TrackedData<Integer> SPINNING_HEAD_TIME = DataTracker.registerData(CopperGolemEntity.class, TrackedDataHandlerRegistry.INTEGER);
    protected static final TrackedData<Boolean> CHASING = DataTracker.registerData(CopperGolemEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    protected static final TrackedData<Byte> REACH_DIRECTION = DataTracker.registerData(CopperGolemEntity.class, TrackedDataHandlerRegistry.BYTE);
    protected static final TrackedData<NbtCompound> POSING = DataTracker.registerData(CopperGolemEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);

    public static final byte REACHING_NONE = 0;
    public static final byte REACHING_UP = 1;
    public static final byte REACHING_DOWN = 2;

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
    public boolean inanimate;

    private int prevReachingTicks;
    private int reachingTicks;

    private final Map<Identifier, BlockInteraction> finders = new HashMap<>();

    @Nullable
    private MineBlockGoal miningGoal;

    CopperGolemEntity(EntityType<CopperGolemEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected void initGoals() {
        miningGoal = new MineBlockGoal(this);
        goalSelector.add(1, new ChaseTargetGoal(this));
        goalSelector.add(2, miningGoal);
        goalSelector.add(2, new VariantSpeedEscapeDangerGoal(this));
        goalSelector.add(2, new PressButtonGoal(this, 14, 30));
        goalSelector.add(3, new VariantSpeedWanderAroundFarGoal(this));
        goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 6));
        goalSelector.add(8, new LookAroundGoal(this));
        targetSelector.add(1, new ActiveTargetGoal<>(this, CatEntity.class, true, true));
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(OXIDATION, 0);
        dataTracker.startTracking(WIGGLING_NOSE_TIME, 0);
        dataTracker.startTracking(SPINNING_HEAD_TIME, 0);
        dataTracker.startTracking(CHASING, false);
        dataTracker.startTracking(REACH_DIRECTION, REACHING_NONE);
        dataTracker.startTracking(POSING, new NbtCompound());
    }

    public void teachInteraction(ItemStack stack, BlockState state, LearnedDuties.Duty duty) {
        BlockInteraction interaction = finders.computeIfAbsent(Registries.ITEM.getId(stack.getItem()), id -> {
            return BlockInteraction.create(this, 14);
        });

        if (interaction instanceof LearnedDuties.Receiver r) {
            r.learn(state, duty);
            wiggleNose();
            spinHead();
            setReachDirection(REACHING_UP);
        }
    }

    public void startMining(BlockState state, BlockPos pos) {
        miningGoal.startMining(state, pos);
    }

    public BlockInteraction getFinder(int maxDistance) {
        return finders.computeIfAbsent(Registries.ITEM.getId(getMainHandStack().getItem()), id -> {
            return BlockInteraction.create(this, maxDistance);
        });
    }

    @Override
    public boolean canPickupItem(ItemStack stack) {
        ItemStack current = getMainHandStack();

        if (!current.isEmpty() && ItemStack.canCombine(current, stack)) {
            return true;
        }

        return stack.isIn(GItems.Tags.COPPER_GOLEM_CAN_PICK_UP);
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
        dataTracker.set(OXIDATION, MathHelper.clamp(oxidation, 0, (Oxidizable.OxidationLevel.values().length - 1) * 100));
    }

    public int getOxidation() {
        return dataTracker.get(OXIDATION);
    }

    public OxidationLevel getDegradationLevel() {
        int oxidation = (int)Math.floor((float)getOxidation() / 100F);

        var levels = Oxidizable.OxidationLevel.values();

        return levels[MathHelper.clamp(oxidation, 0, levels.length - 1)];
    }

    public void setDegradationLevel(OxidationLevel level) {
        setOxidation(level.ordinal() * 100);
    }

    public byte getReachDirection() {
        return dataTracker.get(REACH_DIRECTION);
    }

    public float getReachAmount(float tickDelta) {
        return MathHelper.lerp(tickDelta, prevReachingTicks, reachingTicks);
    }

    public void setReachDirection(byte direction) {
        dataTracker.set(REACH_DIRECTION, direction);
        reachingTicks = direction == REACHING_NONE ? 0 : 200;
    }

    public boolean isChasing() {
        return dataTracker.get(CHASING);
    }

    public void setChasing(boolean chasing) {
        dataTracker.set(CHASING, chasing);
    }

    public double getArmReach() {
        return getWidth() * 2;
    }

    public boolean isWigglingNose() {
        return dataTracker.get(WIGGLING_NOSE_TIME) > 0;
    }

    public void wiggleNose() {
        dataTracker.set(WIGGLING_NOSE_TIME, 10);
    }

    public void expressDissappointment() {
        wiggleNose();
        playSound(GSounds.ENTITY_COPPER_GOLEM_NO, getSoundVolume(), getSoundPitch());
    }

    public int getHeadSpinTime() {
        return dataTracker.get(SPINNING_HEAD_TIME);
    }

    public void spinHead() {
        dataTracker.set(SPINNING_HEAD_TIME, 10);
    }

    @Override
    public float getMovementSpeed() {
        return 0.3F / (1 + getDegradationLevel().ordinal());
    }

    public float getMiningSpeed(BlockState block) {
        ItemStack heldItem = this.getMainHandStack();
        float speed = heldItem.getMiningSpeedMultiplier(block) * 0.67F;
        if (speed > 1) {
            int efficiency = EnchantmentHelper.getEfficiency(this);
            if (efficiency > 0 && !heldItem.isEmpty()) {
                speed += (float)(efficiency * efficiency + 1);
            }
        }
        if (StatusEffectUtil.hasHaste(this)) {
            speed *= 1.0f + (float)(StatusEffectUtil.getHasteAmplifier(this) + 1) * 0.2f;
        }
        if (this.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            speed *= (switch (getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) {
                case 0 -> 0.3f;
                case 1 -> 0.09f;
                case 2 -> 0.0027f;
                default -> 8.1E-4f;
            });
        }
        if (isSubmergedIn(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(this)) {
            speed /= 5F;
        }
        if (!onGround) {
            speed /= 5F;
        }
        return speed;
    }

    @Override
    protected int getNextAirUnderwater(int air) {
        return air;
    }

    @Override
    protected boolean isImmobile() {
        return super.isImmobile()
                || getDegradationLevel() == OxidationLevel.OXIDIZED
                || getReachDirection() != REACHING_NONE
                || (miningGoal != null && miningGoal.canStart());
    }

    @Override
    public boolean isCollidable() {
        return inanimate;
    }

    public boolean isPreoccupied() {
        return reachingTicks > 0 || (miningGoal != null && miningGoal.canStart()) || getHeadSpinTime() > 0;
    }

    @Override
    public void tick() {

        inanimate = getDegradationLevel() == OxidationLevel.OXIDIZED;

        if (inanimate) {
            if (getPosing().isEmpty() && !world.isClient) {
                setPosing(Optional.of(storeAngles()));
            }

            super.tick();

            getPosing().ifPresent(this::loadAngles);
        } else {
            if (getPosing().isPresent() && !world.isClient) {
                setPosing(Optional.empty());
            }

            if (world.isClient && reachingTicks == 0 && getReachDirection() != REACHING_NONE) {
                setReachDirection(REACHING_NONE);
                reachingTicks = 200;
            }

            prevReachingTicks = reachingTicks;
            if (reachingTicks > 0 && --reachingTicks == 0) {
                setReachDirection(REACHING_NONE);
            }

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
        if (stack.isIn(ItemTags.AXES)) {
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
        } else if (stack.isOf(Items.COPPER_INGOT)) {
            if (getHealth() < getMaxHealth()) {
                playSound(SoundEvents.ENTITY_IRON_GOLEM_REPAIR, 1, 1);
                spinHead();
                stack.decrement(1);
                heal(5);
                swingHand(Hand.MAIN_HAND);
                return ActionResult.SUCCESS;
            }

            spinHead();
            playSound(GSounds.ENTITY_COPPER_GOLEM_NO, getSoundVolume(), getSoundPitch());

            return ActionResult.FAIL;
        } else {
            ItemStack heldStack = getStackInHand(Hand.MAIN_HAND);

            if (stack.isEmpty()) {
                if (!heldStack.isEmpty()) {
                    player.getInventory().offerOrDrop(heldStack);

                    equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                    playSound(SoundEvents.ENTITY_ITEM_PICKUP, 1, 1);
                    return ActionResult.SUCCESS;
                }
            } else if (heldStack.isEmpty() || ItemStack.canCombine(heldStack, stack)) {
                ItemStack newStack = stack.split(1);
                newStack.increment(heldStack.getCount());
                equipStack(EquipmentSlot.MAINHAND, newStack);
                playSound(SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, 1, 1);
                handDropChances[EquipmentSlot.MAINHAND.getEntitySlotId()] = 2;
                swingHand(Hand.MAIN_HAND);

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

    public void setPosing(Optional<NbtCompound> posing) {
        NbtCompound tag = new NbtCompound();
        posing.ifPresent(t -> tag.put("posing", t));
        dataTracker.set(POSING, tag);
    }

    public Optional<NbtCompound> getPosing() {
        NbtCompound tag = dataTracker.get(POSING);
        return tag.contains("posing", NbtElement.COMPOUND_TYPE) ? Optional.of(tag.getCompound("posing")) : Optional.empty();
    }

    public void loadAngles(NbtCompound tag) {
        setPitch(tag.getFloat("pitch"));
        bodyYaw = tag.getFloat("bodyYaw");
        prevBodyYaw = bodyYaw;
        stepBobbingAmount = tag.getFloat("stepBobbingAmount");
        limbAnimator.setSpeed(0);
        limbAnimator.updateLimbs(tag.getFloat("limbAngle") - limbAnimator.getPos(), 1);
        handSwingProgress = tag.getFloat("handSwingProgress");
        lastHandSwingProgress = handSwingProgress;
    }

    @Override
    protected void updateLimbs(float posDelta) {
        if (inanimate) {
            limbAnimator.setSpeed(0);
            limbAnimator.updateLimbs(0, 0);
            return;
        }
        super.updateLimbs(posDelta);
    }

    public NbtCompound storeAngles() {
        NbtCompound tag = new NbtCompound();
        tag.putFloat("pitch", getPitch());
        tag.putFloat("bodyYaw", bodyYaw);
        tag.putFloat("stepBobbingAmount", stepBobbingAmount);
        tag.putFloat("limbAngle", limbAnimator.getPos());
        tag.putFloat("handSwingProgress", handSwingProgress);
        return tag;
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
        getPosing().ifPresent(posing -> nbt.put("posing", posing));
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
        setPosing(nbt.contains("posing") ? Optional.of(nbt.getCompound("posing")) : Optional.empty());

        getPosing().ifPresent(this::loadAngles);
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
        if (inanimate) {
            return SoundEvents.BLOCK_COPPER_HIT;
        }
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

        OxidationLevel[] oxidation = new OxidationLevel[] { OxidationLevel.UNAFFECTED };

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

        if (oxidation[0] != OxidationLevel.OXIDIZED || golem.getRandom().nextBoolean()) {
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
