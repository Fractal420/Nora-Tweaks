package me.noramibu.tweaks.mixin;

import me.noramibu.tweaks.category.ModuleDragController;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_LEFT;

@Mixin(value = WPressable.class, remap = false)
public abstract class WModuleDragMixin extends WWidget {
    @Shadow protected boolean pressed;

    @Inject(method = "onMouseClicked", at = @At("HEAD"), require = 0)
    private void nora$onModuleMouseClicked(MouseButtonEvent click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        if (click.button() != MOUSE_BUTTON_LEFT) return;
        if (!mouseOver) return;

        Module module = ModuleDragController.extractModule((WWidget) (Object) this);
        if (module == null) return;

        ModuleDragController.beginPotentialDrag(module, (WWidget) (Object) this, click.x(), click.y());
    }

    @Inject(method = "onMouseReleased", at = @At("HEAD"), cancellable = true, require = 0)
    private void nora$onModuleMouseReleased(MouseButtonEvent click, CallbackInfoReturnable<Boolean> cir) {
        if (click.button() != MOUSE_BUTTON_LEFT) return;

        if (ModuleDragController.consumeSuppressToggle()) {
            pressed = false;
            cir.setReturnValue(true);
            return;
        }

        Module module = ModuleDragController.extractModule((WWidget) (Object) this);
        if (module == null) return;

        if (ModuleDragController.isDragging() || ModuleDragController.cancelClickToggle()) {
            ModuleDragController.endDrag(click.x(), click.y());
            pressed = false;
            cir.setReturnValue(true);
        } else if (ModuleDragController.isPending()) {
            ModuleDragController.cancel();
        }
    }
}
