package dev.wavy.motionblur.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MotionBlurConfig {

    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("motionblur.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static MotionBlurConfig INSTANCE = new MotionBlurConfig();

    public boolean enabled = true;
    public float strength = 50F;
    public boolean exponentialFade = false;

    public static MotionBlurConfig instance() {
        return INSTANCE;
    }

    public static Screen configScreen(Screen parent) {
        return new MotionBlurConfigScreen(parent);
    }

    public static float effectiveBlendFactor() {
        if (!INSTANCE.enabled) return 0F;
        return Math.min(INSTANCE.strength, 99F) / 100F;
    }

    public static void setStrength(float value) {
        INSTANCE.strength = Math.max(0F, Math.min(100F, value));
        save();
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }
        try {
            String json = Files.readString(CONFIG_PATH);
            MotionBlurConfig loaded = GSON.fromJson(json, MotionBlurConfig.class);
            if (loaded != null) INSTANCE = loaded;
        } catch (IOException | RuntimeException e) {
            System.err.println("[MotionBlur] Failed to load config, using defaults: " + e.getMessage());
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(INSTANCE));
        } catch (IOException e) {
            System.err.println("[MotionBlur] Failed to save config: " + e.getMessage());
        }
    }
}
