package moe.plushie.armourers_workshop.api.event;

import moe.plushie.armourers_workshop.core.capability.SkinWardrobe;
import moe.plushie.armourers_workshop.core.menu.SkinSlotType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 时装衣橱事件基类
 */
public abstract class SkinWardrobeEvent {

    protected final SkinWardrobe wardrobe;
    protected final SkinSlotType slotType;
    protected final int slot;
    protected final ItemStack itemStack;

    protected SkinWardrobeEvent(SkinWardrobe wardrobe, SkinSlotType slotType, int slot, ItemStack itemStack) {
        this.wardrobe = wardrobe;
        this.slotType = slotType;
        this.slot = slot;
        this.itemStack = itemStack;
    }

    /**
     * 获取衣橱
     */
    public SkinWardrobe getWardrobe() {
        return wardrobe;
    }

    /**
     * 获取衣橱所属实体
     */
    @Nullable
    public Entity getEntity() {
        return wardrobe.entity();
    }

    /**
     * 获取槽位类型
     */
    public SkinSlotType getSlotType() {
        return slotType;
    }

    /**
     * 获取槽位索引
     */
    public int getSlot() {
        return slot;
    }

    /**
     * 获取时装物品
     */
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * 获取物品的显示名称（自定义名称）
     * @return 如果物品没有设置自定义名称则返回 null
     */
    @Nullable
    public Component getDisplayName() {
        if (itemStack.isEmpty()) {
            return null;
        }
        var customName = itemStack.get(DataComponents.CUSTOM_NAME);
        return customName;
    }

    /**
     * 获取物品的 Lore（描述文本）
     * @return Lore 列表，如果没有则返回空列表
     */
    public List<Component> getLore() {
        if (itemStack.isEmpty()) {
            return new ArrayList<>();
        }
        var lore = itemStack.get(DataComponents.LORE);
        if (lore == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(lore.lines());
    }

    /**
     * 穿戴时装事件 - 当时装被放入衣橱槽位时触发
     * 可以取消此事件来阻止穿戴
     */
    public static class Equip extends SkinWardrobeEvent {

        private boolean cancelled = false;

        public Equip(SkinWardrobe wardrobe, SkinSlotType slotType, int slot, ItemStack itemStack) {
            super(wardrobe, slotType, slot, itemStack);
        }

        /**
         * 是否已取消
         */
        public boolean isCancelled() {
            return cancelled;
        }

        /**
         * 设置取消状态
         * @param cancelled true 表示取消穿戴操作
         */
        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }
    }

    /**
     * 卸下时装事件 - 当时装从衣橱槽位移除时触发
     * 可以取消此事件来阻止卸下
     */
    public static class Unequip extends SkinWardrobeEvent {

        private boolean cancelled = false;

        public Unequip(SkinWardrobe wardrobe, SkinSlotType slotType, int slot, ItemStack itemStack) {
            super(wardrobe, slotType, slot, itemStack);
        }

        /**
         * 是否已取消
         */
        public boolean isCancelled() {
            return cancelled;
        }

        /**
         * 设置取消状态
         * @param cancelled true 表示取消卸下操作
         */
        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }
    }
}
