
package com.daniel.hotbar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HotbarConfig {

    private static final Gson GSON =
            new GsonBuilder().setPrettyPrinting().create();

    private static final Path PATH =
            FabricLoader.getInstance()
                    .getConfigDir()
                    .resolve("hotbar-manager.json");

    public String[] slots = {
            "", "", "", "", "", "", "", "", ""
    };

    public boolean enabled = true;

    public static HotbarConfig load() {
        try {
            if (Files.exists(PATH)) {
                HotbarConfig config = GSON.fromJson(
                        Files.readString(PATH),
                        HotbarConfig.class
                );

                if (config != null &&
                        config.slots != null &&
                        config.slots.length == 9) {
                    for (int i = 0; i < 9; i++) {
                        if (config.slots[i] == null) {
                            config.slots[i] = "";
                        }
                    }
                    return config;
                }
            }
        } catch (Exception ignored) {
        }

        return new HotbarConfig();
    }

    public void save() {
        try {
            Files.createDirectories(PATH.getParent());

            Files.writeString(
                    PATH,
                    GSON.toJson(this),
                    StandardCharsets.UTF_8
            );
        } catch (Exception e) {
            HotbarManagerClient.LOGGER.warn(
                    "Could not save Hotbar Manager settings",
                    e
            );
        }
    }
}
