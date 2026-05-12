package dev.wavy.motionblur.mixin;

import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import dev.wavy.motionblur.MotionBlurRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {

    @Shadow @Final private CrossFrameResourcePool resourcePool;

    @Inject(method = "render", at = @At("TAIL"))
    private void motionblur$afterFrameRender(DeltaTracker tracker, boolean tick, CallbackInfo ci) {
        MotionBlurRenderer.apply(this.resourcePool);
    }
}
