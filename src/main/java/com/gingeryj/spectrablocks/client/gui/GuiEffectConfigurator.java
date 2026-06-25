package com.gingeryj.spectrablocks.client.gui;

import com.gingeryj.spectrablocks.network.ModNetwork;
import com.gingeryj.spectrablocks.network.PacketSetRenderScale;
import com.gingeryj.spectrablocks.tile.TileMicroUniverse;
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
    private final int currentPlanetCount;
    private GuiTextField scaleField;
    private GuiTextField planetField;

    public GuiEffectConfigurator(BlockPos pos, double currentScale) {
        this(pos, currentScale, -1);
    }

    public GuiEffectConfigurator(BlockPos pos, double currentScale, int currentPlanetCount) {
        this.pos = pos;
        this.currentScale = currentScale;
        this.currentPlanetCount = currentPlanetCount;
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

        if (currentPlanetCount >= 0) {
            planetField = new GuiTextField(3, fontRenderer, centerX - 60, centerY + 40, 120, 20);
            planetField.setMaxStringLength(2);
            planetField.setText(String.valueOf(currentPlanetCount));
            planetField.setFocused(false);
        }

        int buttonY = currentPlanetCount >= 0 ? centerY + 72 : centerY + 24;
        buttonList.add(new GuiButton(APPLY, centerX - 102, buttonY, 98, 20,
                I18n.format("gui.spectrablocks.effect_configurator.apply")));
        buttonList.add(new GuiButton(RESET, centerX + 4, buttonY, 98, 20,
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
                centerX, currentPlanetCount >= 0 ? centerY + 100 : centerY + 52, 0x7FAFC8);
        scaleField.drawTextBox();

        if (currentPlanetCount >= 0 && planetField != null) {
            drawCenteredString(fontRenderer, "PlanetCount (0-" + TileMicroUniverse.MAX_PLANET_COUNT + ")",
                    centerX, centerY + 24, 0xA6DFFF);
            planetField.drawTextBox();
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == RESET) {
            scaleField.setText("1.00");
            if (planetField != null) {
                planetField.setText("0");
            }
            sendValues(1.0D, 0);
        } else if (button.id == APPLY) {
            sendValues(parseScale(), parsePlanetCount());
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        scaleField.textboxKeyTyped(typedChar, keyCode);
        if (planetField != null) {
            planetField.textboxKeyTyped(typedChar, keyCode);
        }
        if (keyCode == 28 || keyCode == 156) {
            sendValues(parseScale(), parsePlanetCount());
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        scaleField.mouseClicked(mouseX, mouseY, mouseButton);
        if (planetField != null) {
            planetField.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void updateScreen() {
        scaleField.updateCursorCounter();
        if (planetField != null) {
            planetField.updateCursorCounter();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private double parseScale() {
        try {
            return Double.parseDouble(scaleField.getText().trim());
        } catch (NumberFormatException e) {
            return currentScale;
        }
    }

    private int parsePlanetCount() {
        if (planetField == null) {
            return currentPlanetCount;
        }
        try {
            int val = Integer.parseInt(planetField.getText().trim());
            return TileMicroUniverse.clampPlanetCount(val);
        } catch (NumberFormatException e) {
            return currentPlanetCount;
        }
    }

    private void sendValues(double scale, int planetCount) {
        ModNetwork.CHANNEL.sendToServer(new PacketSetRenderScale(pos, scale, planetCount));
        mc.displayGuiScreen(null);
    }
}