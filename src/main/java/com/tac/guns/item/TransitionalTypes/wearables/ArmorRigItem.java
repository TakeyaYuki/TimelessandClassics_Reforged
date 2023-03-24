package com.tac.guns.item.TransitionalTypes.wearables;

import com.tac.guns.GunMod;
import com.tac.guns.Reference;
import com.tac.guns.common.NetworkRigManager;
import com.tac.guns.common.Rig;
import com.tac.guns.inventory.gear.armor.ArmorRigCapabilityProvider;
import com.tac.guns.inventory.gear.armor.ArmorRigContainerProvider;
import com.tac.guns.inventory.gear.armor.ArmorRigInventoryCapability;
import com.tac.guns.inventory.gear.armor.RigSlotsHandler;
import com.tac.guns.util.CompatUtil;
import com.tac.guns.util.CurioCompatUtil;
import com.tac.guns.util.RigEnchantmentHelper;
import com.tac.guns.util.WearableHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class ArmorRigItem extends Item implements IArmoredRigItem {
    public ArmorRigItem(Properties properties) {
        super(properties);
        numOfRows = 1;
    }

    private final int numOfRows;
    public int getNumOfRows() {
        return this.numOfRows;
    }

    public ArmorRigItem(/*String model, */int rows, Properties properties)
    {
        super(properties);
        //this.armorModelName = model;
        this.numOfRows = rows
        ;
    }
    private ArmorRigContainerProvider containerProvider;
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if(world.isClientSide) return super.use(world, player, hand);
        if(hand != InteractionHand.MAIN_HAND) return InteractionResultHolder.pass(player.getItemInHand(hand));
        containerProvider = new ArmorRigContainerProvider(player.getItemInHand(hand));
        NetworkHooks.openGui((ServerPlayer) player, containerProvider);
        super.use(world, player, hand);
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt)
    {
        if(GunMod.curiosLoaded)
        {
            return createBackpackProvider(stack);
        }
        return new ArmorRigInventoryCapability();
    }
    public static ICapabilityProvider createBackpackProvider(ItemStack stack)
    {
        return new ICapabilityProvider() {
            @NotNull
            @Override
            public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @org.jetbrains.annotations.Nullable Direction side) {

                T provider;
                if(CompatUtil.isCuriohere){
                    provider = CurioCompatUtil.getCurioCapability(cap, stack);
                } else {
                    provider = null;
                }

                if(provider != null){
                    return LazyOptional.of(() -> provider);
                }

                return LazyOptional.empty();
            }
        };
    }

    private WeakHashMap<CompoundTag, Rig> modifiedRigCache = new WeakHashMap<>();

    private Rig rig = new Rig();

    public void setRig(NetworkRigManager.Supplier supplier)
    {
        this.rig = supplier.getRig();
    }

    public Rig getRig()
    {
        return this.rig;
    }

    /*@OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flag) {
       super.addInformation(stack, worldIn, tooltip, flag);

       tooltip.add(new TranslationTextComponent("info.tac.current_armor_amount").append(new TranslationTextComponent(ItemStack.DECIMALFORMAT.format(WearableHelper.GetCurrentDurability(stack))+"")).mergeStyle(TextFormatting.BLUE));
       int scancode = GLFW.glfwGetKeyScancode(InputHandler.ARMOR_REPAIRING.getKeyCode());
       if(GLFW.glfwGetKeyName(InputHandler.ARMOR_REPAIRING.getKeyCode(),scancode) != null)
           tooltip.add((new TranslationTextComponent("info.tac.tac_armor_repair1").append(new TranslationTextComponent(GLFW.glfwGetKeyName(InputHandler.ARMOR_REPAIRING.getKeyCode(), scancode)).mergeStyle(TextFormatting.AQUA)).append(new TranslationTextComponent("info.tac.tac_armor_repair2"))).mergeStyle(TextFormatting.YELLOW));
    }*/

    @Override
    public boolean shouldOverrideMultiplayerNbt() {return true;}

    @Nullable
    @Override
    public CompoundTag getShareTag(ItemStack stack)
    {
        stack.getOrCreateTag();
        CompoundTag nbt = super.getShareTag(stack);
        if (stack.getItem() instanceof ArmorRigItem) {
            RigSlotsHandler itemHandler = (RigSlotsHandler) stack.getCapability(ArmorRigCapabilityProvider.capability).resolve().get();
            nbt.put("storage", itemHandler.serializeNBT());
        }

        return nbt;
    }



    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity)
    {
        return true;
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> stacks)
    {
        if(this.allowdedIn(group))
        {
            ItemStack stack = new ItemStack(this);
            stack.getOrCreateTag();
            WearableHelper.FillDefaults(stack, this.rig);
            stacks.add(stack);
        }
    }

    @Override
    public boolean isBarVisible(ItemStack p_150899_) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack)
    {
        stack.getOrCreateTag();
        Rig modifiedRig = this.getModifiedRig(stack);
        return (int) (13* (WearableHelper.GetCurrentDurability(stack) / (double) RigEnchantmentHelper.getModifiedDurability(stack, modifiedRig)));
    }

    @Override
    public int getBarColor(ItemStack p_150901_) {
        return Objects.requireNonNull(ChatFormatting.AQUA.getColor());
    }

    public Rig getModifiedRig(ItemStack stack)
    {
        CompoundTag tagCompound = stack.getTag();
        if(tagCompound != null && tagCompound.contains("Rig", Tag.TAG_COMPOUND))
        {
            return this.modifiedRigCache.computeIfAbsent(tagCompound, item ->
            {
                if(tagCompound.getBoolean("Custom"))
                {
                    return Rig.create(tagCompound.getCompound("Rig"));
                }
                else
                {
                    Rig gunCopy = this.rig.copy();
                    gunCopy.deserializeNBT(tagCompound.getCompound("Rig"));
                    return gunCopy;
                }
            });
        }
        return this.rig;
    }

    /*@Override
    public ArmorBase getArmorModelName() {
        return this.armorModelName == null ? new ModernArmor() : this.armorModelName;
    }*/
}
