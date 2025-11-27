package com.lx862.quitgame.mixin;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.SplashRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SplashRenderer.class)
public class SplashRendererMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void quitgame$cancelRenderer(GuiGraphics guiGraphics, int screenWidth, Font font, float alpha, CallbackInfo ci) {
        ci.cancel();
    }
}
