package me.noramibu.tweaks.category;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WView;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;

import java.lang.reflect.Field;
import java.util.List;

public class ModuleDragController {
    private static final double DRAG_THRESHOLD = 6.0;

    private static Module module;
    private static String sourceCategoryKey;
    private static WWidget sourceWidget;
    private static double startX, startY;
    private static double mouseX, mouseY;
    private static boolean dragging;
    private static boolean pending;
    private static boolean suppressToggle;
    private static int insertIndex = -1;
    private static String targetCategoryKey;
    private static WWindow targetWindow;

    public static boolean isDragging() {
        return dragging;
    }

    public static boolean isPending() {
        return pending;
    }

    public static Module getModule() {
        return module;
    }

    public static boolean beginPotentialDrag(Module mod, WWidget widget, double mx, double my) {
        if (mod == null || widget == null) return false;
        module = mod;
        sourceWidget = widget;
        startX = mx;
        startY = my;
        mouseX = mx;
        mouseY = my;
        pending = true;
        dragging = false;
        sourceCategoryKey = resolveCategoryKey(widget);
        insertIndex = -1;
        targetCategoryKey = null;
        targetWindow = null;
        return true;
    }

    public static void updateMouse(double mx, double my) {
        mouseX = mx;
        mouseY = my;
        if (!pending && !dragging) return;

        if (pending && !dragging) {
            double dx = mx - startX;
            double dy = my - startY;
            if (dx * dx + dy * dy >= DRAG_THRESHOLD * DRAG_THRESHOLD) {
                dragging = true;
                pending = false;
            }
        }

        if (dragging) {
            resolveDropTarget(mx, my);
        }
    }

    public static boolean cancelClickToggle() {
        return dragging || (pending && (Math.abs(mouseX - startX) > 1 || Math.abs(mouseY - startY) > 1));
    }

    public static void endDrag(double mx, double my) {
        boolean didDrag = dragging;
        if (dragging && module != null) {
            resolveDropTarget(mx, my);
            if (targetCategoryKey != null && targetWindow != null) {
                if (targetCategoryKey.equals(sourceCategoryKey)) {
                    ModuleLayoutManager.reorderWithin(sourceCategoryKey, module, insertIndex);
                } else {
                    ModuleLayoutManager.moveModule(module, sourceCategoryKey, targetCategoryKey, insertIndex);
                }
            }
        }
        if (didDrag) suppressToggle = true;
        reset();
    }

    public static boolean consumeSuppressToggle() {
        if (suppressToggle) {
            suppressToggle = false;
            return true;
        }
        return false;
    }

    public static void cancel() {
        reset();
    }

    private static void reset() {
        module = null;
        sourceWidget = null;
        sourceCategoryKey = null;
        pending = false;
        dragging = false;
        insertIndex = -1;
        targetCategoryKey = null;
        targetWindow = null;
    }

    public static void renderOverlay(GuiRenderer renderer, double mx, double my) {
        if (!dragging || module == null) return;

        GuiTheme theme = GuiThemes.get();
        if (theme == null) return;

        String title = module.title;
        double pad = theme.scale(4);
        double textW = theme.textWidth(title);
        double w = pad + textW + pad;
        double h = pad + theme.textHeight() + pad;
        double x = mx + 12;
        double y = my + 12;

        Color bg = new Color(30, 30, 30, 200);
        Color border = targetCategoryKey != null
            ? new Color(100, 200, 120, 220)
            : new Color(200, 80, 80, 220);
        Color text = new Color(255, 255, 255, 255);

        renderer.quad(x, y, w, h, bg);
        renderer.quad(x, y, 2, h, border);
        renderer.text(title, x + pad, y + pad, text, false);

        if (targetWindow != null && insertIndex >= 0) {
            drawInsertMarker(renderer, targetWindow, insertIndex, border);
        }
    }

    private static void drawInsertMarker(GuiRenderer renderer, WWindow window, int index, Color color) {
        WView view = window.view;
        if (view == null) return;

        List<?> cells = view.cells;
        double markerY;
        double markerX = window.x + 2;
        double markerW = Math.max(4, window.width - 4);

        if (cells == null || cells.isEmpty()) {
            markerY = window.y + window.height * 0.5;
        } else if (index <= 0) {
            WWidget first = cellWidget(cells.get(0));
            markerY = first != null ? first.y : window.y + 20;
        } else if (index >= cells.size()) {
            WWidget last = cellWidget(cells.get(cells.size() - 1));
            markerY = last != null ? last.y + last.height : window.y + window.height - 4;
        } else {
            WWidget at = cellWidget(cells.get(index));
            markerY = at != null ? at.y : window.y + 20;
        }

        renderer.quad(markerX, markerY - 1, markerW, 2, color);
    }

    private static WWidget cellWidget(Object cell) {
        if (cell == null) return null;
        try {
            Field f = cell.getClass().getField("widget");
            Object w = f.get(cell);
            return w instanceof WWidget widget ? widget : null;
        } catch (Throwable t) {
            try {
                Field f = cell.getClass().getDeclaredField("widget");
                f.setAccessible(true);
                Object w = f.get(cell);
                return w instanceof WWidget widget ? widget : null;
            } catch (Throwable ignored) {
                return null;
            }
        }
    }

    private static void resolveDropTarget(double mx, double my) {
        targetCategoryKey = null;
        targetWindow = null;
        insertIndex = -1;

        WWindow best = null;
        String bestKey = null;

        WWidget root = sourceWidget;
        while (root != null && root.parent != null) root = root.parent;

        if (root instanceof WContainer container) {
            findWindowAt(container, mx, my);
        }
    }

    private static void findWindowAt(WContainer container, double mx, double my) {
        for (int i = container.cells.size() - 1; i >= 0; i--) {
            WWidget child = container.cells.get(i).widget();
            if (child instanceof WWindow window) {
                if (isPointInWindow(window, mx, my)) {
                    String key = categoryKeyFromWindow(window);
                    if (key == null) continue;
                    if ("search".equals(key) || "favorites".equals(key)) continue;

                    targetWindow = window;
                    targetCategoryKey = key;
                    insertIndex = computeInsertIndex(window, my);
                    return;
                }
            }
            if (child instanceof WContainer nested) {
                findWindowAt(nested, mx, my);
                if (targetWindow != null) return;
            }
        }
    }

    private static boolean isPointInWindow(WWindow window, double mx, double my) {
        return mx >= window.x && mx <= window.x + window.width
            && my >= window.y && my <= window.y + window.height;
    }

    private static int computeInsertIndex(WWindow window, double my) {
        WView view = window.view;
        if (view == null || view.cells.isEmpty()) return 0;

        int index = view.cells.size();
        for (int i = 0; i < view.cells.size(); i++) {
            WWidget w = view.cells.get(i).widget();
            if (w == null) continue;
            double mid = w.y + w.height / 2.0;
            if (my < mid) {
                index = i;
                break;
            }
        }

        if (sourceCategoryKey != null && sourceCategoryKey.equals(targetCategoryKey) && module != null) {
            int sourceIdx = indexOfModuleInView(view, module);
            if (sourceIdx >= 0 && index > sourceIdx) {
                index--;
            }
        }
        return Math.max(0, index);
    }

    private static int indexOfModuleInView(WView view, Module mod) {
        for (int i = 0; i < view.cells.size(); i++) {
            WWidget w = view.cells.get(i).widget();
            Module m = extractModule(w);
            if (m != null && m.name.equals(mod.name)) return i;
        }
        return -1;
    }

    public static Module extractModule(WWidget widget) {
        if (widget == null) return null;
        try {
            Field f = widget.getClass().getDeclaredField("module");
            f.setAccessible(true);
            Object val = f.get(widget);
            return val instanceof Module m ? m : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static String resolveCategoryKey(WWidget widget) {
        WWidget current = widget;
        while (current != null) {
            if (current instanceof WWindow window) {
                return categoryKeyFromWindow(window);
            }
            current = current.parent;
        }
        return null;
    }

    private static String categoryKeyFromWindow(WWindow window) {
        if (window.id == null || window.id.isEmpty()) {
            try {
                Field titleField = WWindow.class.getDeclaredField("title");
                titleField.setAccessible(true);
                Object t = titleField.get(window);
                if (t instanceof String s && !s.isEmpty()) {
                    for (CustomCategory cat : CustomCategoryManager.getCategories()) {
                        if (cat.name.equals(s)) return ModuleLayoutManager.customKey(cat.name);
                    }
                    return s;
                }
            } catch (Throwable ignored) {
            }
            return null;
        }
        if (window.id.startsWith("custom-")) return window.id;
        return window.id;
    }
}
