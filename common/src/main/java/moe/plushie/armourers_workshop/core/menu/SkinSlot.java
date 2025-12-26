package moe.plushie.armourers_workshop.core.menu;

import moe.plushie.armourers_workshop.api.event.SkinWardrobeEvent;
import moe.plushie.armourers_workshop.compat.core.menu.AbstractContainerSlot;
import moe.plushie.armourers_workshop.core.capability.SkinWardrobe;
import moe.plushie.armourers_workshop.core.utils.Collections;
import moe.plushie.armourers_workshop.core.utils.OpenResourceLocation;
import moe.plushie.armourers_workshop.init.platform.EventManager;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class SkinSlot extends AbstractContainerSlot {

    protected final List<SkinSlotType> slotTypes;
    protected final List<OpenResourceLocation> slotTypeIcons;

    @Nullable
    private SkinWardrobe wardrobe;
    private int slotIndex;

    public SkinSlot(Container inventory, int index, int x, int y, SkinSlotType... slotTypes) {
        super(inventory, index, x, y);
        this.slotTypes = Collections.newList(slotTypes);
        this.slotTypeIcons = Collections.compactMap(slotTypes, SkinSlotType::icon);
    }

    /**
     * 设置衣橱引用，用于触发事件
     */
    public void setWardrobe(@Nullable SkinWardrobe wardrobe, SkinSlotType slotType, int slotIndex) {
        this.wardrobe = wardrobe;
        this.slotIndex = slotIndex;
    }

    public Collection<SkinSlotType> slotTypes() {
        return slotTypes;
    }

    @Override
    protected boolean abi$mayPlace(ItemStack itemStack) {
        // when slot type is not provide, we consider it is an unrestricted slot.
        if (!slotTypes.isEmpty() && !slotTypes.contains(SkinSlotType.byItem(itemStack))) {
            return false;
        }
        if (!container.canPlaceItem(index, itemStack)) {
            return false;
        }

        // 触发 Equip 事件预检查（仅服务端）
        if (wardrobe != null && !slotTypes.isEmpty() && !itemStack.isEmpty()) {
            var entity = wardrobe.entity();
            if (entity != null && !entity.level().isClientSide()) {
                var slotType = slotTypes.iterator().next();
                var equipEvent = new SkinWardrobeEvent.Equip(wardrobe, slotType, slotIndex, itemStack);
                EventManager.post(SkinWardrobeEvent.Equip.class, equipEvent);
                if (equipEvent.isCancelled()) {
                    return false; // 事件被取消，阻止放置
                }
            }
        }

        return true;
    }

    @Override
    protected boolean abi$mayPickup(Player player) {
        // 触发 Unequip 事件预检查（仅服务端）
        if (wardrobe != null && !slotTypes.isEmpty()) {
            var entity = wardrobe.entity();
            var currentItem = abi$getItem();
            if (entity != null && !entity.level().isClientSide() && !currentItem.isEmpty()) {
                var slotType = slotTypes.iterator().next();
                var unequipEvent = new SkinWardrobeEvent.Unequip(wardrobe, slotType, slotIndex, currentItem);
                EventManager.post(SkinWardrobeEvent.Unequip.class, unequipEvent);
                if (unequipEvent.isCancelled()) {
                    return false; // 事件被取消，阻止取出
                }
            }
        }

        return super.abi$mayPickup(player);
    }

    @Override
    protected OpenResourceLocation abi$noItemIcon() {
        int size = slotTypeIcons.size();
        if (size > 0) {
            return slotTypeIcons.get((int) ((System.currentTimeMillis() / 1000L) % size));
        }
        return null;
    }
}
