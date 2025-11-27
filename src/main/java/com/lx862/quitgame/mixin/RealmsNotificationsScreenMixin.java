package com.lx862.quitgame.mixin;

import com.lx862.quitgame.QuitGame;
import com.mojang.realmsclient.gui.screens.RealmsNotificationsScreen;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RealmsNotificationsScreen.class)
public class RealmsNotificationsScreenMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if(!QuitGame.unlocked) ci.cancel();
    }
}
