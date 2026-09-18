package me.noramibu.tweaks.mixin;

import me.noramibu.tweaks.category.ModuleLayoutManager;
import meteordevelopment.meteorclient.gui.screens.ModulesScreen;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(targets = "meteordevelopment.meteorclient.gui.screens.ModulesScreen$WCategoryController", remap = false)
public abstract class ModulesScreenDragMixin {
    @Redirect(
        method = "init",
        at = @At(
            value = "INVOKE",
            target = "Lmeteordevelopment/meteorclient/systems/modules/Modules;getGroup(Lmeteordevelopment/meteorclient/systems/modules/Category;)Ljava/util/List;"
        ),
        require = 0
    )
    private List<Module> nora$orderedGroup(Modules modules, Category category) {
        return ModuleLayoutManager.getModulesForDefaultCategory(category);
    }
}
