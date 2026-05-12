package dev.wavy.motionblur.config.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ToggleRow extends AbstractWidget {

    private static final int COLOR_ON = 0xFF80FF80;
    private static final int COLOR_OFF = 0xFFBBBBBB;

    private final Supplier<Boolean> getter;
    private final Consumer<Boolean> setter;
    private final Supplier<Component> descSupplier;

    public ToggleRow(int x, int y, int w, int h, Component label,
                     Supplier<Boolean> getter, Consumer<Boolean> setter,
                     Supplier<Component> descSupplier) {
        super(x, y, w, h, label);
        this.getter = getter;
        this.setter = setter;
        this.descSupplier = descSupplier;
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

        boolean on = getter.get();
        Component state = Component.literal(on ? "ON" : "OFF");
        int stateW = font.width(state);
        int stateX = getX() + getWidth() - stateW - 8;
        g.drawString(font, state, stateX, textY, on ? COLOR_ON : COLOR_OFF, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent e, boolean bl) {
        if (!this.active || !this.visible) return false;
        if (!this.isValidClickButton(e.buttonInfo())) return false;
        if (!this.isMouseOver(e.x(), e.y())) return false;
        setter.accept(!getter.get());
        this.playDownSound(Minecraft.getInstance().getSoundManager());
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput out) {
        defaultButtonNarrationText(out);
    }
}
