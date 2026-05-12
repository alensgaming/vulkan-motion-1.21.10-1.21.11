package dev.wavy.motionblur.config.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class Button extends AbstractWidget {
    private final Runnable onClick;

    public Button(int x, int y, int w, int h, Component text, Runnable onClick) {
        super(x, y, w, h, text);
        this.onClick = onClick;
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        Font font = Minecraft.getInstance().font;
        boolean hov = this.isHovered();
        int r = 2;

        Renderer2D.fillRoundedRect(g, getX(), getY(), getWidth(), getHeight(), r, hov ? MainColors.BUTTON_FILL_HOVER : MainColors.BUTTON_FILL);
        Renderer2D.fillRoundedRectOutline(g, getX(), getY(), getWidth(), getHeight(), r, 1, hov ? MainColors.OUTLINE_WHITE_HOVERED : MainColors.OUTLINE_WHITE);
        Renderer2D.fillRoundedRectOutline(g, getX() - 1, getY() - 1, getWidth() + 2, getHeight() + 2, r, 1, MainColors.OUTLINE_BLACK);

        int tx = getX() + getWidth() / 2 - font.width(getMessage()) / 2;
        int ty = getY() + (getHeight() - font.lineHeight) / 2 + 1;
        g.drawString(font, getMessage(), tx, ty, 0xFFFFFFFF, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent e, boolean bl) {
        if (!this.active || !this.visible) return false;
        if (!this.isValidClickButton(e.buttonInfo())) return false;
        if (!this.isMouseOver(e.x(), e.y())) return false;
        this.playDownSound(Minecraft.getInstance().getSoundManager());
        onClick.run();
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput out) {
        defaultButtonNarrationText(out);
    }
}
