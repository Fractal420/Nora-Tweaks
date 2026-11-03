package me.noramibu.tweaks.mixin.rejects;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "anticope.rejects.MeteorRejectsAddon", remap = false)
public class MeteorRejectsAddonMixin {
    private static final Logger LOG = LogUtils.getLogger();

    // Use @WrapOperation instead of @Redirect for MixinExtras compatibility
    @WrapOperation(
        method = "onInitialize",
        at = @At(
            value = "INVOKE",
            target = "Lmeteordevelopment/meteorclient/systems/modules/Modules;add(Lmeteordevelopment/meteorclient/systems/modules/Module;)V"
        )
    )
    private void redirectModulesAdd(Modules modules, Module module, Operation<Void> original) {
        String className = module.getClass().getSimpleName();
        if ("OreSim".equals(className) || "AutoFarm".equals(className)) {
            return;
        }
        original.call(modules, module);
    }

    @WrapOperation(
        method = "onInitialize",
        at = @At(
            value = "INVOKE",
            target = "Lmeteordevelopment/meteorclient/commands/Commands;add(Lmeteordevelopment/meteorclient/commands/Command;)V"
        )
    )
    private static void redirectCommandsAdd(Command command, Operation<Void> original) {
        String className = command.getClass().getSimpleName();
        if ("SeedCommand".equals(className) || "LocateCommand".equals(className)) {
            return;
        }
        original.call(command);
    }
}
