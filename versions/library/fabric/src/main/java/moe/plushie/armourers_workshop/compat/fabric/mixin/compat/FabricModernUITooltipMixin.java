package moe.plushie.armourers_workshop.compat.fabric.mixin.compat;

import moe.plushie.armourers_workshop.api.annotation.Available;
import moe.plushie.armourers_workshop.compat.client.event.AbstractRenderItemTooltipEvent;
import moe.plushie.armourers_workshop.init.event.client.ItemTooltipEvent;
import moe.plushie.armourers_workshop.init.platform.EventManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Available("[1.20, )")
@Pseudo
@Mixin(targets = "icyllis.modernui.mc.TooltipRenderer")
public class FabricModernUITooltipMixin {

    @Inject(method = "drawTooltip", at = @At("TAIL"), remap = false, require = 0)
    private void aw2$afterDrawTooltip(ItemStack itemStack, GuiGraphics gr, List<ClientTooltipComponent> list,
                                       int mouseX, int mouseY, Font font, int screenWidth, int screenHeight,
                                       float partialX, float partialY, ClientTooltipPositioner positioner,
                                       CallbackInfo ci) {
        if (itemStack == null || itemStack.isEmpty()) {
            return;
        }
        // calculate tooltip size.
        int w = 0;
        int h = list.size() == 1 ? -2 : 0;
        for (var tooltip : list) {
            w = Math.max(w, tooltip.getWidth(font));
            h += tooltip.getHeight();
        }
        // calculate position (same as vanilla).
        int x = mouseX + 12;
        int y = mouseY - 12;
        if (x + w > screenWidth) {
            x -= 28 + w;
        }
        if (y + h + 6 > screenHeight) {
            y = screenHeight - h - 6;
        }
        EventManager.post(ItemTooltipEvent.Render.class, AbstractRenderItemTooltipEvent.create(itemStack, x, y, w, h, screenWidth, screenHeight, mouseX, mouseY, 0, gr));
    }
}
