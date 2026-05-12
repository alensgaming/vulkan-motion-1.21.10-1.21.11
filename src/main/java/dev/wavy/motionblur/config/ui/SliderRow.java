package dev.wavy.motionblur.config.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class SliderRow extends AbstractWidget {

    private final float min;
    private final float max;
    private final float step;

    private final Supplier<Float> getter;
    private final Consumer<Float> setter;
    private final Supplier<Component> descSupplier;
    private final Function<Float, String> formatter;

    private boolean dragging = false;

    public SliderRow(int x, int y, int w, int h, Component label, float min, float max, float step,
                     Supplier<Float> getter, Consumer<Float> setter, Supplier<Component> descSupplier) {
        this(x, y, w, h, label, min, max, step, getter, setter, descSupplier, v -> String.format("%.1fx", v));
    }

    public SliderRow(int x, int y, int w, int h, Component label, float min, float max, float step,
                     Supplier<Float> getter, Consumer<Float> setter, Supplier<Component> descSupplier,
                     Function<Float, String> formatter) {
        super(x, y, w, h, label);
        this.min = min;
        this.max = max;
        this.step = step;
        this.getter = getter;
        this.setter = setter;
        this.descSupplier = descSupplier;
        this.formatter = formatter;
    }

    public Component description() {
        return descSupplier.get();
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        Font font = Minecraft.getInstance().font;
        int r = 2;
        boolean hov = this.isHovered();

        Renderer2D.fillRoundedRect(g, getX(), getY(), getWidth(), getHeight(), r, MainColors.ROW_FILL);
        Renderer2D.fillRoundedRectOutline(g, getX(), getY(), getWidth(), getHeight(), r, 1, hov ? MainColors.OUTLINE_WHITE_HOVERED : MainColors.OUTLINE_WHITE);
        Renderer2D.fillRoundedRectOutline(g, getX() - 1, getY() - 1, getWidth() + 2, getHeight() + 2, r, 1, MainColors.OUTLINE_BLACK);

        int labelX = getX() + 8;
        int textY = getY() + (getHeight() - font.lineHeight) / 2 + 1;
        g.drawString(font, getMessage(), labelX, textY, 0xFFFFFFFF, false);

        float v = getter.get();
        String vs = formatter.apply(v);

        int valueW = font.width(vs);
        int valueX = getX() + getWidth() - valueW - 8;
        g.drawString(font, vs, valueX, textY, 0xFFFFFFFF, false);

        int trackX = getX() + 170;
        int trackW = Math.max(10, valueX - 12 - trackX);
        int trackY = getY() + getHeight() / 2 - 2;
        int trackH = 4;

        g.fill(trackX, trackY, trackX + trackW, trackY + trackH, 0x40FFFFFF);

        float t = (v - min) / (max - min);
        t = Mth.clamp(t, 0f, 1f);

        int knobX = trackX + Math.round(t * (trackW - 10));
        int knobY = getY() + getHeight() / 2 - 5;

        int knobColor = hov ? 0xFFFFFFFF : 0xE0FFFFFF;
        Renderer2D.fillRoundedRect(g, knobX, knobY, 10, 10, 2, knobColor);
        Renderer2D.fillRoundedRectOutline(g, knobX, knobY, 10, 10, 2, 1, MainColors.OUTLINE_BLACK);
    }

    private void setFromMouse(double mx) {
        int valueX = getX() + getWidth() - 8;
        int trackX = getX() + 170;
        int trackW = Math.max(10, valueX - 12 - trackX);

        float t = (float) ((mx - trackX) / (double) (trackW - 10));
        t = Mth.clamp(t, 0f, 1f);

        float raw = min + t * (max - min);
        float snapped = step <= 0 ? raw : (Math.round(raw / step) * step);
        snapped = Mth.clamp(snapped, min, max);

        setter.accept(snapped);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent e, boolean bl) {
        if (!this.active || !this.visible) return false;
        if (!this.isValidClickButton(e.buttonInfo())) return false;
        if (!this.isMouseOver(e.x(), e.y())) return false;
        dragging = true;
        setFromMouse(e.x());
        this.playDownSound(Minecraft.getInstance().getSoundManager());
        return true;
    }

    @Override
    protected void onDrag(MouseButtonEvent e, double dx, double dy) {
        if (!dragging) return;
        setFromMouse(e.x());
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent e) {
        dragging = false;
        return super.mouseReleased(e);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput out) {
        defaultButtonNarrationText(out);
    }
}
