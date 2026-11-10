
package com.daniel.hotbar;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public final class HotbarManagerClient
        implements ClientModInitializer {

    public static final Logger LOGGER =
            LoggerFactory.getLogger("Hotbar Manager");

    public static HotbarConfig CONFIG;

    private final Map<String, Integer> lastCounts =
            new HashMap<>();

    private final LinkedHashSet<String> pending =
            new LinkedHashSet<>();

    private LocalPlayer observed;
    private int cooldown = 0;

    @Override
    public void onInitializeClient() {
        CONFIG = HotbarConfig.load();

        ClientTickEvents.END_CLIENT_TICK.register(
                this::tick
        );
    }

    public static String itemId(ItemStack stack) {
        if (stack.isEmpty()) {
            return "";
        }

        return BuiltInRegistries.ITEM
                .getKey(stack.getItem())
                .toString();
    }

    private void tick(Minecraft client) {
        LocalPlayer player = client.player;

        if (player == null || client.gameMode == null) {
            observed = null;
            lastCounts.clear();
            pending.clear();
            return;
        }

        if (observed != player) {
            observed = player;
            lastCounts.clear();
            pending.clear();

            lastCounts.putAll(countItems(player));
            return;
        }

        Map<String, Integer> current =
                countItems(player);

        for (Map.Entry<String, Integer> entry
                : current.entrySet()) {

            String id = entry.getKey();
            int previous =
                    lastCounts.getOrDefault(id, 0);

            if (entry.getValue() > previous
                    && assignedSlot(id) >= 0) {
                pending.add(id);
            }
        }

        lastCounts.clear();
        lastCounts.putAll(current);

        if (!CONFIG.enabled
                || client.screen != null
                || player.containerMenu != player.inventoryMenu) {
            return;
        }

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        if (pending.isEmpty()) {
            return;
        }

        cooldown = 5;

        Iterator<String> iterator =
                pending.iterator();

        String id = iterator.next();
        iterator.remove();

        int target = assignedSlot(id);

        if (target < 0) {
            return;
        }

        if (id.equals(itemId(
                player.getInventory().getItem(target)))) {
            return;
        }

        int source = -1;

        for (int i = 0; i < 36; i++) {
            if (i != target
                    && id.equals(itemId(
                    player.getInventory().getItem(i)))) {
                source = i;
                break;
            }
        }

        if (source < 0) {
            return;
        }

        int menuSlot = source < 9
                ? 36 + source
                : source;

        client.gameMode.handleInventoryMouseClick(
                player.inventoryMenu.containerId,
                menuSlot,
                target,
                ClickType.SWAP,
                player
        );
    }

    private Map<String, Integer> countItems(
            LocalPlayer player) {

        Map<String, Integer> counts =
                new HashMap<>();

        for (int i = 0; i < 36; i++) {
            ItemStack stack =
                    player.getInventory().getItem(i);

            if (!stack.isEmpty()) {
                counts.merge(
                        itemId(stack),
                        stack.getCount(),
                        Integer::sum
                );
            }
        }

        return counts;
    }

    private int assignedSlot(String id) {
        for (int i = 0; i < 9; i++) {
            if (id.equals(CONFIG.slots[i])) {
                return i;
            }
        }

        return -1;
    }
}
