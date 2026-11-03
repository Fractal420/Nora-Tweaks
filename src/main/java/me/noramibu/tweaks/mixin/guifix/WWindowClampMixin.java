package me.noramibu.tweaks.mixin.guifix;

import me.noramibu.tweaks.guifix.WindowPositionMemory;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

@Mixin(WWindow.class)
public abstract class WWindowClampMixin {
    // Use @WrapOperation instead of @Redirect for MixinExtras compatibility
    @WrapOperation(
        method = "onCalculateWidgetPositions",
        at = @At(
            value = "FIELD",
            target = "Lmeteordevelopment/meteorclient/gui/widgets/containers/WWindow;height:D",
            opcode = 180
        ),
        require = 0
    )
    private double noraGuiFix$useVisibleHeightForClamp(WWindow window, Operation<Double> original) {
        try {
            return WindowPositionMemory.effectiveHeight(window);
        } catch (Throwable ignored) {
            return original.call(window);
        }
    }
}
