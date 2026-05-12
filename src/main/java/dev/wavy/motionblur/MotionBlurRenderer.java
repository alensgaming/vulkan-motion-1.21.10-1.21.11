package dev.wavy.motionblur;

import dev.wavy.motionblur.config.MotionBlurConfig;
import dev.wavy.motionblur.mixin.PostChainAccessor;
import dev.wavy.motionblur.mixin.PostPassAccessor;
import dev.wavy.motionblur.mixin.ShaderManagerAccessor;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.Identifier;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class MotionBlurRenderer {

    private static final Identifier EFFECT_ID =
            Identifier.fromNamespaceAndPath(MotionBlurMod.ID, "motion_blur");
    private static final String UNIFORM_BLOCK = "MotionBlurUniforms";
    private static final int UBO_SIZE = 16;
    private static final int UBO_USAGE = 130;

    private static PostChain cachedChain;
    private static GpuBuffer blendFactorUbo;
    private static Method createBufferMethod;
    private static boolean loadFailed;
    private static boolean runtimeFailed;



    private MotionBlurRenderer() {}

    public static void apply(GraphicsResourceAllocator allocator) {
        if (runtimeFailed) return;

        Minecraft client = Minecraft.getInstance();

        float blend = MotionBlurMod.getBlendFactor();
        if (blend <= 0.0F) return;

        MotionBlurConfig cfg = MotionBlurConfig.instance();
        float expMode = cfg.exponentialFade ? 1.0F : 0.0F;

        PostChain chain = loadChain(client);
        if (chain == null) return;

        try {
            installUboIfNeeded(chain);
            writeBlend(blend, expMode);
            chain.process(client.getMainRenderTarget(), allocator);
        } catch (Throwable t) {
            runtimeFailed = true;
            System.err.println("[MotionBlur] Post-effect runtime failure, disabling for this session: " + t);
        }
    }

    private static PostChain loadChain(Minecraft client) {
        if (loadFailed) return null;
        try {
            net.minecraft.client.renderer.ShaderManager.CompilationCache cache =
                    ((ShaderManagerAccessor) client.getShaderManager()).getCompilationCache();
            if (cache == null) return null;
            PostChain chain = cache.getOrLoadPostChain(EFFECT_ID, LevelTargetBundle.MAIN_TARGETS);
            if (chain != cachedChain) {
                cachedChain = chain;
                blendFactorUbo = null;
            }
            return cachedChain;
        } catch (Exception e) {
            loadFailed = true;
            System.err.println("[MotionBlur] Failed to load post-effect: " + e.getMessage());
            return null;
        }
    }

    private static void installUboIfNeeded(PostChain chain) {
        if (blendFactorUbo == null) {
            blendFactorUbo = createUbo(UNIFORM_BLOCK, UBO_SIZE);
            if (blendFactorUbo == null) return;
        }

        List<PostPass> passes = ((PostChainAccessor) chain).getPasses();
        if (passes.isEmpty()) return;

        Map<String, GpuBuffer> uniforms = ((PostPassAccessor) passes.get(0)).getCustomUniforms();
        GpuBuffer current = uniforms.get(UNIFORM_BLOCK);
        if (current == blendFactorUbo) return;

        GpuBuffer displaced = uniforms.put(UNIFORM_BLOCK, blendFactorUbo);
        if (displaced != null && displaced != blendFactorUbo) {
            displaced.close();
        }
    }

    private static void writeBlend(float blend, float expMode) {
        if (blendFactorUbo == null) return;
        try (GpuBuffer.MappedView view =
                     RenderSystem.getDevice().createCommandEncoder().mapBuffer(blendFactorUbo, false, true)) {
            Std140Builder.intoBuffer(view.data())
                    .putVec4(blend, 0.0F, expMode, 0.0F);
        }
    }

    private static GpuBuffer createUbo(String debugName, int sizeBytes) {
        Object device = RenderSystem.getDevice();
        Supplier<String> label = () -> MotionBlurMod.ID + ":" + debugName;
        try {
            if (createBufferMethod == null) {
                createBufferMethod = device.getClass().getMethod(
                        "createBuffer", Supplier.class, int.class, long.class);
            }
            return (GpuBuffer) createBufferMethod.invoke(device, label, UBO_USAGE, (long) sizeBytes);
        } catch (ReflectiveOperationException e) {
            loadFailed = true;
            System.err.println("[MotionBlur] createBuffer reflection failed: " + e.getMessage());
            return null;
        }
    }
}
