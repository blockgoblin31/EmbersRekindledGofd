package com.rekindled.embers.blockentity;

import com.rekindled.embers.Embers;
import com.rekindled.embers.RegistryManager;
import com.rekindled.embers.api.tile.IExtraCapabilityInformation;
import com.rekindled.embers.datagen.EmbersItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AlchemyReagentPedestalBlockEntity extends AlchemyPedestalBlockEntity {
    public int active = 0;

    public AlchemyReagentPedestalBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(RegistryManager.ALCHEMY_REAGENT_PEDESTAL_BLOCK_ENTITY.get(), pPos, pBlockState);
        inventory = new ItemStackHandler(1) {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return stack.is(EmbersItemTags.REAGENTS);
            }

            @Override
            protected void onContentsChanged(int slot) {
                AlchemyReagentPedestalBlockEntity.this.setChanged();
            }
        };
    }

    public ItemStack getContents() {
        return inventory.getStackInSlot(0);
    }

    public boolean isValid() {
        return !inventory.getStackInSlot(0).isEmpty();
    }

    public static void clientTick(Level level, BlockPos pos, BlockState blockState, AlchemyReagentPedestalBlockEntity blockEntity) {
        blockEntity.active--;
    }

    public boolean isActive() {
        return active > 0;
    }

    public void setActive(int time) {
        active = time;
    }

    @Override
    public boolean hasCapabilityDescription(Capability<?> capability) {
        return capability == ForgeCapabilities.ITEM_HANDLER;
    }

    @Override
    public void addCapabilityDescription(List<Component> strings, Capability<?> capability, Direction facing) {
        strings.add(IExtraCapabilityInformation.formatCapability(EnumIOType.INPUT, Embers.MODID + ".tooltip.goggles.item", null));
    }
}
