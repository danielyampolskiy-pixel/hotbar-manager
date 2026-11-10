
package com.daniel.hotbar;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public final class HotbarConfigScreen extends Screen {

    private final Screen parent;
    private final List<String> available =
            new ArrayList<>();

    public HotbarConfigScreen(Screen parent) {
        super(Component.literal("Hotbar Slot Manager"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        available.clear();
        available.add("");

        LinkedHashSet<String> unique =
                new LinkedHashSet<>();

        if (Minecraft.getInstance().player != null) {
            for (int i = 0; i < 36; i++) {
                ItemStack stack = Minecraft.getInstance()
                        .player.getInventory().getItem(i);

                if (!stack.isEmpty()) {
                    unique.add(
                            HotbarManagerClient.itemId(stack)
                    );
                }
            }
        }

        available.addAll(unique);

        int x = width / 2 - 145;
        int startY = height / 2 - 108;

        addRenderableWidget(
                Button.builder(
                        Component.literal(
                                HotbarManagerClient.CONFIG.enabled
                                        ? "Automatic sorting: ON"
                                        : "Automatic sorting: OFF"
                        ),
                        button -> {
                            HotbarManagerClient.CONFIG.enabled =
                                    !HotbarManagerClient.CONFIG.enabled;

                            HotbarManagerClient.CONFIG.save();
                            rebuildWidgets();
                        }
                ).bounds(x, startY, 290, 20).build()
        );

        for (int i = 0; i < 9; i++) {
            final int index = i;
            int y = startY + 24 + i * 20;

            addRenderableWidget(
                    Button.builder(
                            label(index),
                            button -> {
                                String current =
                                        HotbarManagerClient.CONFIG.slots[index];

                                int next =
                                        available.indexOf(current) + 1;

                                if (next >= available.size()) {
                                    next = 0;
                                }

                                HotbarManagerClient.CONFIG.slots[index] =
                                        available.get(next);

                                HotbarManagerClient.CONFIG.save();

                                button.setMessage(label(index));
                            }
                    ).bounds(x, y, 290, 19).build()
            );
        }

        addRenderableWidget(
                Button.builder(
                        Component.literal("Done"),
                        button -> onClose()
                ).bounds(
                        width / 2 - 70,
                        startY + 208,
                        140,
                        20
                ).build()
        );
    }

    private Component label(int index) {
        String item =
                HotbarManagerClient.CONFIG.slots[index];

        return Component.literal(
                "Slot " + (index + 1) + ": " +
                        (item.isBlank()
                                ? "Any / Unassigned"
                                : item) +
                        " (click to change)"
        );
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }
}
