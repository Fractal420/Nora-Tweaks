package me.noramibu.tweaks.mixin;

import me.noramibu.tweaks.category.ModuleDragController;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = WidgetScreen.class, remap = false)
public abstract class WidgetScreenDragMixin {
    @Inject(method = "mouseMoved", at = @At("HEAD"), require = 0)
    private void nora$dragMouseMoved(double mouseX, double mouseY, CallbackInfo ci) {
        if (ModuleDragController.isPending() || ModuleDragController.isDragging()) {
            try {
                double s = meteordevelopment.meteorclient.MeteorClient.mc.getWindow().getGuiScale();
                ModuleDragController.updateMouse(mouseX * s, mouseY * s);
            } catch (Throwable t) {
                ModuleDragController.updateMouse(mouseX, mouseY);
            }
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), require = 0)
    private void nora$dragMouseReleased(MouseButtonEvent click, CallbackInfoReturnable<Boolean> cir) {
        if (ModuleDragController.isDragging() || ModuleDragController.isPending()) {
            try {
                double s = meteordevelopment.meteorclient.MeteorClient.mc.getWindow().getGuiScale();
                ModuleDragController.endDrag(click.x() * s, click.y() * s);
            } catch (Throwable t) {
                ModuleDragController.endDrag(click.x(), click.y());
            }
        }
    }

    @Inject(method = "runAfterRenderTasks", at = @At("HEAD"), require = 0)
    private void nora$dragOverlay(CallbackInfo ci) {
        if (!ModuleDragController.isDragging()) return;
        try {
            GuiRenderer renderer = getStaticRenderer();
            if (renderer == null) return;
            var mc = meteordevelopment.meteorclient.MeteorClient.mc;
            double s = mc.getWindow().getGuiScale();
            double mx = mc.mouseHandler.xpos() * s;
            double my = mc.mouseHandler.ypos() * s;
            ModuleDragController.renderOverlay(renderer, mx, my);
        } catch (Throwable ignored) {
        }
    }

    private static GuiRenderer getStaticRenderer() {
        try {
            java.lang.reflect.Field f = WidgetScreen.class.getDeclaredField("RENDERER");
            f.setAccessible(true);
            Object val = f.get(null);
            return val instanceof GuiRenderer r ? r : null;
        } catch (Throwable t) {
            return null;
        }
    }
}
