package me.noramibu.tweaks.modules;

import me.noramibu.tweaks.NoraTweaks;
import me.noramibu.tweaks.gui.screens.CategoryManagerScreen;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.client.Minecraft;

/**
 * GUI-only module: left-click (or keybind) opens the Category Manager screen.
 * Activation is always cancelled so the module never stays "on".
 */
public class CategoryManagerModule extends Module {
    public CategoryManagerModule() {
        super(NoraTweaks.CATEGORY, "category-manager", "Opens a screen to manage custom module categories.");
    }

    /** Open the manager UI. Safe to call from click handlers or onActivate. */
    public static void openScreen() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;
        GuiTheme theme = GuiThemes.get();
        if (theme == null) return;
        mc.execute(() -> mc.setScreenAndShow(new CategoryManagerScreen(theme)));
    }

    @Override
    public void onActivate() {
        // Keybind / any activation path: open UI then turn off.
        toggle();
        openScreen();
    }
}
