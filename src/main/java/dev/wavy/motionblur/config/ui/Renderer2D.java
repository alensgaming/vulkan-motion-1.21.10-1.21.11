package dev.wavy.motionblur.config.ui;

import net.minecraft.client.gui.GuiGraphics;

public final class Renderer2D {
    private Renderer2D() {}

    public static void fillRoundedRect(GuiGraphics g, int x, int y, int w, int h, int r, int color) {
        if (w <= 0 || h <= 0) return;
        r = Math.max(0, Math.min(r, Math.min(w / 2, h / 2)));
        if (r == 0) {
            g.fill(x, y, x + w, y + h, color);
            return;
        }

        g.fill(x + r, y, x + w - r, y + h, color);
        g.fill(x, y + r, x + r, y + h - r, color);
        g.fill(x + w - r, y + r, x + w, y + h - r, color);

        fillCorner(g, x + r, y + r, r, color, Corner.TL);
        fillCorner(g, x + w - r - 1, y + r, r, color, Corner.TR);
        fillCorner(g, x + r, y + h - r - 1, r, color, Corner.BL);
        fillCorner(g, x + w - r - 1, y + h - r - 1, r, color, Corner.BR);
    }

    public static void fillRoundedRectOutline(GuiGraphics g, int x, int y, int w, int h, int r, int thickness, int color) {
        for (int i = 0; i < thickness; i++) {
            strokeRoundedRect1(g, x - i, y - i, w + i * 2, h + i * 2, r, color);
        }
    }

    private static void strokeRoundedRect1(GuiGraphics g, int x, int y, int w, int h, int r, int color) {
        if (w <= 0 || h <= 0) return;
        r = Math.max(0, Math.min(r, Math.min(w / 2, h / 2)));

        if (r == 0) {
            g.fill(x, y, x + w, y + 1, color);
            g.fill(x, y + h - 1, x + w, y + h, color);
            g.fill(x, y, x + 1, y + h, color);
            g.fill(x + w - 1, y, x + w, y + h, color);
            return;
        }

        g.fill(x + r, y, x + w - r, y + 1, color);
        g.fill(x + r, y + h - 1, x + w - r, y + h, color);

        g.fill(x, y + r, x + 1, y + h - r, color);
        g.fill(x + w - 1, y + r, x + w, y + h - r, color);

        strokeCorner(g, x + r, y + r, r, color, Corner.TL);
        strokeCorner(g, x + w - r - 1, y + r, r, color, Corner.TR);
        strokeCorner(g, x + r, y + h - r - 1, r, color, Corner.BL);
        strokeCorner(g, x + w - r - 1, y + h - r - 1, r, color, Corner.BR);
    }

    private enum Corner { TL, TR, BL, BR }

    private static void fillCorner(GuiGraphics g, int cx, int cy, int r, int color, Corner c) {
        int rr = r * r;
        for (int dy = 0; dy <= r; dy++) {
            for (int dx = 0; dx <= r; dx++) {
                int dd = dx * dx + dy * dy;
                if (dd > rr) continue;

                int px = switch (c) {
                    case TL, BL -> cx - dx;
                    case TR, BR -> cx + dx;
                };
                int py = switch (c) {
                    case TL, TR -> cy - dy;
                    case BL, BR -> cy + dy;
                };
                g.fill(px, py, px + 1, py + 1, color);
            }
        }
    }

    private static void strokeCorner(GuiGraphics g, int cx, int cy, int r, int color, Corner c) {
        int rr = r * r;
        int rri = (r - 1) * (r - 1);
        for (int dy = 0; dy <= r; dy++) {
            for (int dx = 0; dx <= r; dx++) {
                int dd = dx * dx + dy * dy;
                if (dd > rr || dd < rri) continue;

                int px = switch (c) {
                    case TL, BL -> cx - dx;
                    case TR, BR -> cx + dx;
                };
                int py = switch (c) {
                    case TL, TR -> cy - dy;
                    case BL, BR -> cy + dy;
                };
                g.fill(px, py, px + 1, py + 1, color);
            }
        }
    }
}
