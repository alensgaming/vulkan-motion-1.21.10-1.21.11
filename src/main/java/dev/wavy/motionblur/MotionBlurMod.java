package dev.wavy.motionblur;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.wavy.motionblur.config.MotionBlurConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class MotionBlurMod implements ClientModInitializer {

    public static final String ID = "motionblur";

    private static KeyMapping toggleKey;

    @Override
    public void onInitializeClient() {
        MotionBlurConfig.load();

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.motionblur.toggle",
                InputConstants.Type.KEYSYM,
                InputConstants.UNKNOWN.getValue(),
                KeyMapping.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.consumeClick()) {
                MotionBlurConfig cfg = MotionBlurConfig.instance();
                cfg.enabled = !cfg.enabled;
                MotionBlurConfig.save();
                if (client.player != null) {
                    client.player.displayClientMessage(
                            Component.literal("Motion Blur: " + (cfg.enabled ? "ON" : "OFF")),
                            true
                    );
                }
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    ClientCommandManager.literal("motionblur")
                            .then(ClientCommandManager.argument("percent", IntegerArgumentType.integer(0, 100))
                                    .executes(ctx -> changeAmount(
                                            ctx.getSource(),
                                            IntegerArgumentType.getInteger(ctx, "percent"))))
                            .executes(ctx -> openMenu())
            );
        });
    }

    private static int changeAmount(FabricClientCommandSource src, int amount) {
        MotionBlurConfig.setStrength(amount);
        src.sendFeedback(Component.literal("Motion Blur: " + amount + "%"));
        return amount;
    }

    private static int openMenu() {
        Minecraft client = Minecraft.getInstance();
        client.execute(() -> client.setScreen(MotionBlurConfig.configScreen(client.screen)));
        return 1;
    }

    public static float getBlendFactor() {
        return MotionBlurConfig.effectiveBlendFactor();
    }
}
