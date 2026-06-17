package com.gingeryj.spectrablocks.client.gui;

import com.gingeryj.spectrablocks.network.ModNetwork;
import com.gingeryj.spectrablocks.network.PacketSetRenderScale;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;

public class GuiEffectConfigurator extends GuiScreen {

    private static final int APPLY = 0;
    private static final int RESET = 1;

    private final BlockPos pos;
    private final double currentScale;
    private GuiTextField scaleField;

    public GuiEffectConfigurator(BlockPos pos, double currentScale) {
        this.pos = pos;
        this.currentScale = currentScale;
    }

    @Override
    public void initGui() {
        int centerX = width / 2;
        int centerY = height / 2;
        buttonList.clear();

        scaleField = new GuiTextField(2, fontRenderer, centerX - 60, centerY - 8, 120, 20);
        scaleField.setMaxStringLength(8);
        scaleField.setText(String.format("%.2f", currentScale));
        scaleField.setFocused(true);

        buttonList.add(new GuiButton(APPLY, centerX - 102, centerY + 24, 98, 20,
                I18n.format("gui.spectrablocks.effect_configurator.apply")));
        buttonList.add(new GuiButton(RESET, centerX + 4, centerY + 24, 98, 20,
                I18n.format("gui.spectrablocks.effect_configurator.reset")));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int centerX = width / 2;
        int centerY = height / 2;

        drawCenteredString(fontRenderer, I18n.format("gui.spectrablocks.effect_configurator.title"),
                centerX, centerY - 52, 0xE6F7FF);
        drawCenteredString(fontRenderer, "RenderScale", centerX, centerY - 24, 0xA6DFFF);
        drawCenteredString(fontRenderer, I18n.format("gui.spectrablocks.effect_configurator.range"),
                centerX, centerY + 52, 0x7FAFC8);
        scaleField.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == RESET) {
            scaleField.setText("1.00");
            sendScale(1.0D);
        } else if (button.id == APPLY) {
            sendScale(parseScale());
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        scaleField.textboxKeyTyped(typedChar, keyCode);
        if (keyCode == 28 || keyCode == 156) {
            sendScale(parseScale());
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        scaleField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        scaleField.updateCursorCounter();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private double parseScale() {
        try {
            return Double.parseDouble(scaleField.getText().trim());
        } catch (NumberFormatException ignored) {
            return currentScale;
        }
    }

    private void sendScale(double scale) {
        ModNetwork.CHANNEL.sendToServer(new PacketSetRenderScale(pos, scale));
        mc.displayGuiScreen(null);
    }
}
