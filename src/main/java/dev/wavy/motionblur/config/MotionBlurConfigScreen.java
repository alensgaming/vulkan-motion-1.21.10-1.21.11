package dev.wavy.motionblur.config;

import dev.wavy.motionblur.config.ui.Button;
import dev.wavy.motionblur.config.ui.MainColors;
import dev.wavy.motionblur.config.ui.Renderer2D;
import dev.wavy.motionblur.config.ui.SliderRow;
import dev.wavy.motionblur.config.ui.ToggleRow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class MotionBlurConfigScreen extends Screen {

    private final Screen parent;

    private ToggleRow enabledRow;
    private SliderRow strengthRow;
    private ToggleRow exponentialFadeRow;

    private Button backBtn;
    private Button undoBtn;
    private Button resetBtn;
    private Button saveBtn;

    private boolean originalEnabled;
    private float originalStrength;
    private boolean originalExponentialFade;

    private Component focusedDesc = Component.empty();
    private Component focusedTitle = Component.empty();

    public MotionBlurConfigScreen(Screen parent) {
        super(Component.literal("Motion Blur"));
        this.parent = parent;
    }

    private void snapshot() {
        MotionBlurConfig c = MotionBlurConfig.instance();
        originalEnabled = c.enabled;
        originalStrength = c.strength;
        originalExponentialFade = c.exponentialFade;
    }

    @Override
    protected void init() {
        snapshot();

        int pad = 18;
        int panelX = pad;
        int panelY = pad;
        int panelW = this.width - pad * 2;
        int panelH = this.height - pad * 2;

        int dividerX = panelX + (int) (panelW * 0.66);

        int leftX = panelX + 18;
        int leftY = panelY + 54;
        int leftW = dividerX - leftX - 18;

        int rowH = 22;
        int gap = 10;

        enabledRow = new ToggleRow(
                leftX,
                leftY,
                leftW,
                rowH,
                Component.literal("Enabled"),
                () -> MotionBlurConfig.instance().enabled,
                v -> MotionBlurConfig.instance().enabled = v,
                () -> Component.literal("Turn the motion blur effect on or off without losing your slider settings.")
        );

        strengthRow = new SliderRow(
                leftX,
                leftY + (rowH + gap),
                leftW,
                rowH,
                Component.literal("Strength"),
                0.0f, 99.0f, 1.0f,
                () -> MotionBlurConfig.instance().strength,
                v -> MotionBlurConfig.instance().strength = v,
                () -> Component.literal("How strongly each frame blends into the next. Higher = longer trails. 0 = no blur."),
                v -> Math.round(v) + "%"
        );

        exponentialFadeRow = new ToggleRow(
                leftX,
                leftY + (rowH + gap) * 2,
                leftW,
                rowH,
                Component.literal("Exponential Fade"),
                () -> MotionBlurConfig.instance().exponentialFade,
                v -> MotionBlurConfig.instance().exponentialFade = v,
                () -> Component.literal("OFF = linear blending (steady trails). ON = exponential curve — trails linger longer and feel smokier at the same strength.")
        );

        this.addRenderableWidget(enabledRow);
        this.addRenderableWidget(strengthRow);
        this.addRenderableWidget(exponentialFadeRow);

        int btnH = 18;
        int btnW = 58;
        int btnY = panelY + panelH - btnH - 10;

        backBtn = new Button(panelX + 10, panelY + 10, 52, 18, Component.literal("Back"), () -> Minecraft.getInstance().setScreen(parent));

        saveBtn = new Button(panelX + panelW - btnW - 10, btnY, btnW, btnH, Component.literal("Save"), this::save);
        resetBtn = new Button(panelX + panelW - btnW - 10 - (btnW + 8), btnY, btnW, btnH, Component.literal("Reset"), this::reset);
        undoBtn = new Button(panelX + panelW - btnW - 10 - (btnW + 8) * 2, btnY, btnW, btnH, Component.literal("Undo"), this::undo);

        this.addRenderableWidget(backBtn);
        this.addRenderableWidget(undoBtn);
        this.addRenderableWidget(resetBtn);
        this.addRenderableWidget(saveBtn);
    }

    private void undo() {
        MotionBlurConfig c = MotionBlurConfig.instance();
        c.enabled = originalEnabled;
        c.strength = originalStrength;
        c.exponentialFade = originalExponentialFade;
    }

    private void reset() {
        MotionBlurConfig c = MotionBlurConfig.instance();
        c.enabled = true;
        c.strength = 50F;
        c.exponentialFade = false;
    }

    private void save() {
        MotionBlurConfig.save();
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, this.width, this.height, 0xA0000000);

        int pad = 18;
        int panelX = pad;
        int panelY = pad;
        int panelW = this.width - pad * 2;
        int panelH = this.height - pad * 2;

        int r = 2;

        Renderer2D.fillRoundedRect(g, panelX, panelY, panelW, panelH, r, MainColors.PANEL_FILL);
        Renderer2D.fillRoundedRectOutline(g, panelX, panelY, panelW, panelH, r, 1, MainColors.OUTLINE_WHITE);
        Renderer2D.fillRoundedRectOutline(g, panelX - 1, panelY - 1, panelW + 2, panelH + 2, r, 1, MainColors.OUTLINE_BLACK);

        int titleY = panelY + 16;
        g.drawString(this.font, this.title, this.width / 2 - this.font.width(this.title) / 2, titleY, 0xFFFFFFFF, false);

        int dividerX = panelX + (int) (panelW * 0.66);
        g.fill(dividerX, panelY + 42, dividerX + 1, panelY + panelH - 36, 0x40FFFFFF);

        Component tab = Component.literal("General");
        int tabW = 150;
        int tabH = 20;
        int tabX = panelX + panelW / 2 - tabW / 2;
        int tabY = panelY + 34;

        Renderer2D.fillRoundedRect(g, tabX, tabY, tabW, tabH, r, 0x30000000);
        Renderer2D.fillRoundedRectOutline(g, tabX, tabY, tabW, tabH, r, 1, MainColors.OUTLINE_WHITE);
        Renderer2D.fillRoundedRectOutline(g, tabX - 1, tabY - 1, tabW + 2, tabH + 2, r, 1, MainColors.OUTLINE_BLACK);
        g.drawString(this.font, tab, tabX + tabW / 2 - this.font.width(tab) / 2, tabY + 6, 0xFFFFFFFF, false);

        updateFocus(mouseX, mouseY);

        int rightX = dividerX + 14;
        int rightY = panelY + 54;
        int rightW = panelX + panelW - rightX - 14;
        int rightH = panelY + panelH - rightY - 42;

        Renderer2D.fillRoundedRect(g, rightX, rightY, rightW, rightH, r, 0x28000000);
        Renderer2D.fillRoundedRectOutline(g, rightX, rightY, rightW, rightH, r, 1, MainColors.OUTLINE_BLACK);
        Renderer2D.fillRoundedRectOutline(g, rightX + 1, rightY + 1, rightW - 2, rightH - 2, r, 1, MainColors.OUTLINE_WHITE);

        if (focusedTitle != null) {
            int tx = rightX + rightW / 2 - this.font.width(focusedTitle) / 2;
            g.drawString(this.font, focusedTitle, tx, rightY + 10, 0xFFFFFFFF, false);
        }

        if (focusedDesc != null && !focusedDesc.getString().isEmpty()) {
            int textX = rightX + 12;
            int textY = rightY + 28;
            int wrapW = rightW - 24;

            List<net.minecraft.util.FormattedCharSequence> lines = this.font.split(focusedDesc, wrapW);
            int max = Math.min(lines.size(), 10);
            for (int i = 0; i < max; i++) {
                g.drawString(this.font, lines.get(i), textX, textY + i * (this.font.lineHeight + 2), 0xFFFFFFFF, false);
            }
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void updateFocus(int mouseX, int mouseY) {
        focusedTitle = Component.literal("Strength");
        focusedDesc = strengthRow.description();

        if (enabledRow.isMouseOver(mouseX, mouseY)) {
            focusedTitle = Component.literal("Enabled");
            focusedDesc = enabledRow.description();
        } else if (strengthRow.isMouseOver(mouseX, mouseY)) {
            focusedTitle = Component.literal("Strength");
            focusedDesc = strengthRow.description();
        } else if (exponentialFadeRow.isMouseOver(mouseX, mouseY)) {
            focusedTitle = Component.literal("Exponential Fade");
            focusedDesc = exponentialFadeRow.description();
        }
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
