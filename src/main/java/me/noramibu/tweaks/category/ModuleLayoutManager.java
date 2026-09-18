package me.noramibu.tweaks.category;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.noramibu.tweaks.NoraTweaks;
import me.noramibu.tweaks.events.CustomCategoriesChangedEvent;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ModuleLayoutManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE = new File(MeteorClient.FOLDER, "nora-tweaks-module-layout.json");

    private static final Map<String, String> overrides = new HashMap<>();
    private static final Map<String, List<String>> orders = new LinkedHashMap<>();

    public static void init() {
        load();
    }

    public static String getDisplayCategory(Module module) {
        if (module == null) return null;
        String override = overrides.get(module.name);
        if (override != null) return override;
        return module.category != null ? module.category.name : null;
    }

    public static boolean hasOverride(Module module) {
        return module != null && overrides.containsKey(module.name);
    }

    public static List<Module> getModulesForDefaultCategory(Category category) {
        if (category == null) return new ArrayList<>();
        String catName = category.name;

        List<Module> result = new ArrayList<>();
        for (Module module : Modules.get().getGroup(category)) {
            try {
                if (meteordevelopment.meteorclient.systems.config.Config.get().hiddenModules.get().contains(module)) {
                    continue;
                }
            } catch (Throwable ignored) {
            }
            String display = getDisplayCategory(module);
            if (catName.equals(display)) {
                result.add(module);
            }
        }

        for (Map.Entry<String, String> entry : overrides.entrySet()) {
            if (!catName.equals(entry.getValue())) continue;
            Module module = Modules.get().get(entry.getKey());
            if (module == null) continue;
            try {
                if (meteordevelopment.meteorclient.systems.config.Config.get().hiddenModules.get().contains(module)) {
                    continue;
                }
            } catch (Throwable ignored) {
            }
            if (module.category != null && module.category.name.equals(catName)) continue;
            if (!result.contains(module)) result.add(module);
        }

        List<String> order = orders.get(catName);
        if (order != null && !order.isEmpty()) {
            result.sort((a, b) -> {
                int ia = order.indexOf(a.name);
                int ib = order.indexOf(b.name);
                if (ia < 0) ia = Integer.MAX_VALUE;
                if (ib < 0) ib = Integer.MAX_VALUE;
                if (ia != ib) return Integer.compare(ia, ib);
                return a.title.compareToIgnoreCase(b.title);
            });
        }

        return result;
    }

    public static List<Module> orderCustomModules(CustomCategory category, List<Module> modules) {
        if (modules == null || modules.isEmpty()) return modules;
        String key = customKey(category.name);
        List<String> order = orders.get(key);
        if (order == null || order.isEmpty()) {
            modules.sort(switch (category.sortOrder) {
                case WEIGHT -> java.util.Comparator
                    .comparingInt((Module m) -> CustomCategoryManager.getModuleWeight(m, category))
                    .thenComparing(m -> m.title);
                case Z_TO_A -> java.util.Comparator.comparing((Module m) -> m.title).reversed();
                default -> java.util.Comparator.comparing(m -> m.title);
            });
            return modules;
        }

        modules.sort((a, b) -> {
            int ia = order.indexOf(a.name);
            int ib = order.indexOf(b.name);
            if (ia < 0) ia = Integer.MAX_VALUE;
            if (ib < 0) ib = Integer.MAX_VALUE;
            if (ia != ib) return Integer.compare(ia, ib);
            return a.title.compareToIgnoreCase(b.title);
        });
        return modules;
    }

    public static void moveModule(Module module, String fromCategoryKey, String toCategoryKey, int insertIndex) {
        if (module == null || toCategoryKey == null) return;

        boolean toCustom = isCustomKey(toCategoryKey);
        boolean fromCustom = fromCategoryKey != null && isCustomKey(fromCategoryKey);
        String toName = stripCustomKey(toCategoryKey);
        String fromName = fromCategoryKey != null ? stripCustomKey(fromCategoryKey) : null;

        if (fromCustom && fromName != null) {
            CustomCategory fromCat = findCustom(fromName);
            if (fromCat != null) {
                List<ModuleConfig> configs = getConfigs(fromCat);
                configs.removeIf(mc -> mc.moduleName.equals(module.name));
            }
            removeFromOrder(fromCategoryKey, module.name);
        } else if (fromName != null) {
            removeFromOrder(fromName, module.name);
        }

        if (toCustom) {
            CustomCategory toCat = findCustom(toName);
            if (toCat != null) {
                List<ModuleConfig> configs = getConfigs(toCat);
                if (configs.stream().noneMatch(mc -> mc.moduleName.equals(module.name))) {
                    configs.add(new ModuleConfig(module.name));
                }
            }
            overrides.put(module.name, toName);
            insertIntoOrder(toCategoryKey, module.name, insertIndex);
        } else {
            for (CustomCategory cat : CustomCategoryManager.getCategories()) {
                List<ModuleConfig> configs = getConfigs(cat);
                configs.removeIf(mc -> mc.moduleName.equals(module.name));
            }
            if (module.category != null && module.category.name.equals(toName)) {
                overrides.remove(module.name);
            } else {
                overrides.put(module.name, toName);
            }
            insertIntoOrder(toName, module.name, insertIndex);
        }

        save();
        CustomCategoryManager.save();
        postChange();
    }

    public static void reorderWithin(String categoryKey, Module module, int insertIndex) {
        if (module == null || categoryKey == null) return;
        insertIntoOrder(categoryKey, module.name, insertIndex);

        if (isCustomKey(categoryKey)) {
            CustomCategory cat = findCustom(stripCustomKey(categoryKey));
            if (cat != null) {
                List<String> order = orders.get(categoryKey);
                if (order != null) {
                    List<ModuleConfig> configs = getConfigs(cat);
                    for (int i = 0; i < order.size(); i++) {
                        String name = order.get(i);
                        for (ModuleConfig mc : configs) {
                            if (mc.moduleName.equals(name)) {
                                mc.weight = i * 10;
                                break;
                            }
                        }
                    }
                    cat.sortOrder = SortOrder.WEIGHT;
                }
            }
        }

        save();
        CustomCategoryManager.save();
        postChange();
    }

    public static String categoryKeyForWindow(String windowId) {
        if (windowId == null) return null;
        if (windowId.startsWith("custom-")) return windowId;
        return windowId;
    }

    public static boolean isCustomKey(String key) {
        return key != null && key.startsWith("custom-");
    }

    public static String stripCustomKey(String key) {
        if (key == null) return null;
        if (key.startsWith("custom-")) return key.substring("custom-".length());
        return key;
    }

    public static String customKey(String name) {
        return "custom-" + name;
    }

    private static void insertIntoOrder(String key, String moduleName, int insertIndex) {
        List<String> order = orders.computeIfAbsent(key, k -> new ArrayList<>());
        order.remove(moduleName);
        if (insertIndex < 0 || insertIndex > order.size()) {
            order.add(moduleName);
        } else {
            order.add(insertIndex, moduleName);
        }
    }

    private static void removeFromOrder(String key, String moduleName) {
        List<String> order = orders.get(key);
        if (order != null) order.remove(moduleName);
    }

    private static CustomCategory findCustom(String name) {
        if (name == null) return null;
        return CustomCategoryManager.getCategories().stream()
            .filter(c -> c.name.equals(name))
            .findFirst()
            .orElse(null);
    }

    private static List<ModuleConfig> getConfigs(CustomCategory category) {
        return CustomCategoryManager.getOrCreateConfigs(category);
    }

    private static void postChange() {
        if (MeteorClient.EVENT_BUS != null) {
            MeteorClient.EVENT_BUS.post(CustomCategoriesChangedEvent.INSTANCE);
        }
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc == null) return;
            Object current = currentScreen(mc);
            if (current == null) return;
            if (!current.getClass().getName().contains("ModulesScreen")) return;
            mc.execute(() -> {
                try {
                    meteordevelopment.meteorclient.gui.GuiTheme theme =
                        meteordevelopment.meteorclient.gui.GuiThemes.get();
                    if (theme == null) return;
                    net.minecraft.client.gui.screens.Screen next = theme.modulesScreen();
                    if (next != null) mc.setScreenAndShow(next);
                } catch (Throwable ignored) {
                }
            });
        } catch (Throwable ignored) {
        }
    }

    private static Object currentScreen(net.minecraft.client.Minecraft mc) {
        try {
            Object gui = mc.getClass().getField("gui").get(mc);
            if (gui != null) {
                try {
                    return gui.getClass().getMethod("screen").invoke(gui);
                } catch (NoSuchMethodException e) {
                    try {
                        return gui.getClass().getMethod("getScreen").invoke(gui);
                    } catch (NoSuchMethodException ignored) {
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        try {
            return mc.getClass().getMethod("getScreen").invoke(mc);
        } catch (Throwable ignored) {
        }
        try {
            java.lang.reflect.Field f = mc.getClass().getField("screen");
            return f.get(mc);
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static void save() {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(FILE), StandardCharsets.UTF_8)) {
            LayoutData data = new LayoutData(overrides, orders);
            GSON.toJson(data, writer);
        } catch (IOException e) {
            NoraTweaks.LOG.error("Failed to save module layout", e);
        }
    }

    private static void load() {
        try {
            if (!FILE.exists()) return;
            String json = new String(Files.readAllBytes(FILE.toPath()), StandardCharsets.UTF_8);
            LayoutData data = GSON.fromJson(json, LayoutData.class);
            if (data == null) return;
            if (data.overrides != null) overrides.putAll(data.overrides);
            if (data.orders != null) orders.putAll(data.orders);
        } catch (IOException e) {
            NoraTweaks.LOG.error("Failed to load module layout", e);
        }
    }

    private static class LayoutData {
        Map<String, String> overrides;
        Map<String, List<String>> orders;

        LayoutData(Map<String, String> overrides, Map<String, List<String>> orders) {
            this.overrides = overrides;
            this.orders = orders;
        }
    }
}
