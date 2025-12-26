package moe.plushie.armourers_workshop.compat.fabric.mixin;

import moe.plushie.armourers_workshop.api.annotation.Available;
import moe.plushie.armourers_workshop.compat.client.event.AbstractRenderItemTooltipEvent;
import moe.plushie.armourers_workshop.compat.fabric.AbstractFabricTooltipComponent;
import moe.plushie.armourers_workshop.init.event.client.ItemTooltipEvent;
import moe.plushie.armourers_workshop.init.platform.EventManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Available("[1.20, 1.22)")
@Mixin(GuiGraphics.class)
public class FabricItemTooltipMixin {

    @Unique
    private static ItemStack aw2$tooltipStack;

    @Unique
    private static boolean aw2$rendered;

    // Render preview directly at the entry point - this runs before any sub-method that ModernUI might cancel.
    @Inject(method = "renderTooltip(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V", at = @At("HEAD"))
    private void aw2$renderTooltipPre(Font font, ItemStack itemStack, int mouseX, int mouseY, CallbackInfo ci) {
        aw2$tooltipStack = itemStack;
        aw2$rendered = false;
        // Render preview immediately at the entry point.
        var graphics = GuiGraphics.class.cast(this);
        var screenWidth = graphics.guiWidth();
        var screenHeight = graphics.guiHeight();
        // Get tooltip components to calculate size.
        var screen = net.minecraft.client.Minecraft.getInstance().screen;
        var tooltips = Screen.getTooltipFromItem(net.minecraft.client.Minecraft.getInstance(), itemStack);
        int w = 0;
        for (var tooltip : tooltips) {
            w = Math.max(w, font.width(tooltip));
        }
        int h = tooltips.size() == 1 ? -2 : 0;
        h += tooltips.size() * 10;
        // calculate position (same as vanilla).
        int x = mouseX + 12;
        int y = mouseY - 12;
        if (x + w > screenWidth) {
            x -= 28 + w;
        }
        if (y + h + 6 > screenHeight) {
            y = screenHeight - h - 6;
        }
        aw2$rendered = true;
        EventManager.post(ItemTooltipEvent.Render.class, AbstractRenderItemTooltipEvent.create(itemStack, x, y, w, h, screenWidth, screenHeight, mouseX, mouseY, 0, graphics));
    }

    @Inject(method = "renderTooltip(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V", at = @At("TAIL"))
    private void aw2$renderTooltipPost(Font font, ItemStack itemStack, int mouseX, int mouseY, CallbackInfo ci) {
        aw2$tooltipStack = null;
        aw2$rendered = false;
    }

    // Fallback for non-ItemStack tooltips.
    @Inject(method = "renderTooltipInternal", at = @At("HEAD"))
    private void aw2$renderTooltip(Font font, List<ClientTooltipComponent> tooltips, int mouseX, int mouseY, ClientTooltipPositioner positioner, CallbackInfo ci) {
        // Skip if already rendered at entry point.
        if (aw2$rendered) {
            return;
        }
        // Try text-based lookup for non-ItemStack tooltips.
        var itemStack = AbstractFabricTooltipComponent.deattach(tooltips);
        if (itemStack == null) {
            return;
        }
        aw2$rendered = true;
        int w = 0;
        int h = tooltips.size() == 1 ? -2 : 0;
        for (var tooltip : tooltips) {
            w = Math.max(w, tooltip.getWidth(font));
            h += tooltip.getHeight();
        }
        var graphics = GuiGraphics.class.cast(this);
        var screenWidth = graphics.guiWidth();
        var screenHeight = graphics.guiHeight();
        var position = positioner.positionTooltip(screenWidth, screenHeight, mouseX, mouseY, w, h);
        EventManager.post(ItemTooltipEvent.Render.class, AbstractRenderItemTooltipEvent.create(itemStack, position.x(), position.y(), w, h, screenWidth, screenHeight, mouseX, mouseY, 0, graphics));
    }
}
