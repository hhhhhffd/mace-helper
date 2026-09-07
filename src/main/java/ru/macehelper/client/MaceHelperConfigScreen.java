package ru.macehelper.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class MaceHelperConfigScreen extends Screen {
    private final Screen parent;
    private EditBox firstColorField;
    private EditBox secondColorField;
    private EditBox durationField;
    private Button enabledButton;
    private boolean enabled;
    private String errorMessage = "";

    public MaceHelperConfigScreen(Screen parent) {
        super(Component.translatable("screen.mace_helper.title"));
        this.parent = parent;
        this.enabled = MaceHelperClient.getConfig().isEnabled();
    }

    @Override
    protected void init() {
        int left = this.width / 2 - 100;

        firstColorField = new EditBox(this.font, left, 72, 200, 20, Component.translatable("screen.mace_helper.first_color"));
        firstColorField.setValue(MaceHelperClient.getConfig().getFirstColorHex());
        firstColorField.setMaxLength(7);
        firstColorField.setFilter(value -> value.isEmpty() || value.matches("#[0-9a-fA-F]{0,6}"));
        this.addRenderableWidget(firstColorField);

        secondColorField = new EditBox(this.font, left, 122, 200, 20, Component.translatable("screen.mace_helper.second_color"));
        secondColorField.setValue(MaceHelperClient.getConfig().getSecondColorHex());
        secondColorField.setMaxLength(7);
        secondColorField.setFilter(value -> value.isEmpty() || value.matches("#[0-9a-fA-F]{0,6}"));
        this.addRenderableWidget(secondColorField);

        durationField = new EditBox(this.font, left, 172, 200, 20, Component.translatable("screen.mace_helper.duration"));
        durationField.setValue(Double.toString(MaceHelperClient.getConfig().getDurationSeconds()));
        durationField.setMaxLength(12);
        durationField.setFilter(value -> value.isEmpty() || value.matches("[0-9]*(\\.[0-9]*)?"));
        this.addRenderableWidget(durationField);

        enabledButton = Button.builder(enabledLabel(), button -> {
            enabled = !enabled;
            button.setMessage(enabledLabel());
        }).bounds(left, 212, 200, 20).build();
        this.addRenderableWidget(enabledButton);

        this.addRenderableWidget(Button.builder(Component.translatable("screen.mace_helper.reset"), button -> resetFields())
                .bounds(left - 105, 252, 100, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("screen.mace_helper.cancel"), button -> close())
                .bounds(left + 5, 252, 95, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("screen.mace_helper.apply"), button -> save())
                .bounds(left + 105, 252, 95, 20)
                .build());
    }

    private Component enabledLabel() {
        return Component.translatable("screen.mace_helper.enabled", enabled
                ? Component.translatable("screen.mace_helper.on")
                : Component.translatable("screen.mace_helper.off"));
    }

    private void resetFields() {
        MaceHelperConfig defaults = MaceHelperConfig.defaults();
        firstColorField.setValue(defaults.getFirstColorHex());
        secondColorField.setValue(defaults.getSecondColorHex());
        durationField.setValue(Double.toString(defaults.getDurationSeconds()));
        enabled = defaults.isEnabled();
        enabledButton.setMessage(enabledLabel());
        errorMessage = "";
    }

    private void save() {
        MaceHelperConfig config = MaceHelperClient.getConfig();
        MaceHelperConfig.ApplyResult result = config.apply(
                firstColorField.getValue(),
                secondColorField.getValue(),
                durationField.getValue(),
                enabled
        );

        if (!result.success()) {
            errorMessage = result.error();
            return;
        }

        if (!config.save()) {
            errorMessage = Component.translatable("screen.mace_helper.save_error").getString();
            return;
        }

        MaceHelperClient.onConfigChanged();
        close();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 25, 0xFFFFFFFF);
        graphics.drawString(this.font, Component.translatable("screen.mace_helper.first_color"), firstColorField.getX(), 58, 0xFFFFFFFF);
        graphics.drawString(this.font, Component.translatable("screen.mace_helper.second_color"), secondColorField.getX(), 108, 0xFFFFFFFF);
        graphics.drawString(this.font, Component.translatable("screen.mace_helper.duration"), durationField.getX(), 158, 0xFFFFFFFF);
        graphics.drawString(this.font, Component.translatable("screen.mace_helper.seconds"), durationField.getX() + 205, 178, 0xFFAAAAAA);

        if (!errorMessage.isEmpty()) {
            graphics.drawCenteredString(this.font, errorMessage, this.width / 2, 286, 0xFFFF5555);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        close();
    }

    private void close() {
        this.minecraft.setScreen(parent);
    }
}
