package com.lx862.quitgame.mixin;

import com.lx862.quitgame.ReorderableSplashText;
import com.lx862.quitgame.SplashTextCharacter;
import com.lx862.quitgame.QuitGame;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
    @Shadow private boolean fading;
    @Shadow private long fadeInStart;
    @Unique
    private static ReorderableSplashText splash;
    @Unique
    private double mouseX;
    @Unique
    public float startX = 0;
    @Unique
    public float startY = 0;
    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        if(splash == null) {
            SplashRenderer splashRenderer = Minecraft.getInstance().getSplashManager().getSplash();
            if(splashRenderer != null) {
                splash = new ReorderableSplashText(((SplashRendererAccessorMixin)splashRenderer).getSplash());
            } else {
                splash = new ReorderableSplashText("MISSINGNO");
            }
        }
        startX = (width / 2.0F) + 123F;
        startY = 55;
        for(SplashTextCharacter splashTextCharacter : new ArrayList<>(splash.chars)) {
            splashTextCharacter.setStartPos(startX, startY);
        }
        quitgame$positionCharacters(true);
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void hideWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        for(String keyword : QuitGame.keywords) {
            if(splash.startsWith(keyword)) {
                QuitGame.unlocked = true;
            }
        }

        if(QuitGame.unlocked) {
            QuitGame.unlockedAlpha = Mth.clamp(QuitGame.unlockedAlpha + (delta / 20F), 0.0F, 1.0F);
        }

        for(GuiEventListener child : children()) {
            if(child instanceof Button) {
                ComponentContents msg = ((Button)child).getMessage().getContents();
                if(child instanceof PlainTextButton) continue; // Copyright
                if(msg instanceof TranslatableContents) {
                    if(((TranslatableContents) msg).getKey().contains("quit")) {
                        continue;
                    }
                }

                ((Button) child).visible = QuitGame.unlocked;
                ((Button) child).setAlpha(QuitGame.unlockedAlpha);
            }
        }
    }


    @Inject(method = "render", at = @At("TAIL"))
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        QuitGame.scale = 1.8F * 100.0F / (float)(getFont().width(splash.text) + 32);

        float scale = (float)QuitGame.scale - Mth.abs(Mth.sin((float)(Util.getMillis() % 1000L) / 1000.0F * 6.2831855F) * (0.1F * ((float)QuitGame.scale / 1.8F)));
        quitgame$positionCharacters(false);

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(startX, startY);
        guiGraphics.pose().scale(scale, scale);
        for(SplashTextCharacter splashTextCharacter : new ArrayList<>(splash.chars)) {
            guiGraphics.pose().pushMatrix();
            splashTextCharacter.render(guiGraphics, delta / 3f, quitgame$getAlpha(), getFont());
            guiGraphics.pose().popMatrix();
        }

        guiGraphics.pose().popMatrix();

//        for(CharacterRenderer characterRenderer : new ArrayList<>(QuitGame.splash.chars)) {
//            characterRenderer.renderBoundary(guiGraphics, delta / 3f, textRenderer);
//        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean isDoubleClick) {
        for(SplashTextCharacter splashTextCharacter : splash.chars) {
            if(splashTextCharacter.hovered(mouseButtonEvent.x(), mouseButtonEvent.y())) {
                splashTextCharacter.dragged();
                return super.mouseClicked(mouseButtonEvent, isDoubleClick);
            }
        }
        return super.mouseClicked(mouseButtonEvent, isDoubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
        for(SplashTextCharacter splashTextCharacter : splash.chars) {
            splashTextCharacter.released();
        }
        return super.mouseReleased(mouseButtonEvent);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent mouseButtonEvent, double mouseX, double mouseY) {
        this.mouseX = mouseButtonEvent.x();
        return super.mouseDragged(mouseButtonEvent, mouseX, mouseY);
    }

    @Unique
    public void quitgame$positionCharacters(boolean absolute) {
        QuitGame.scale = 1.8F * (100.0F / (float)(getFont().width(splash.text) + 32));

        double strLength = getFont().width(splash.text) * QuitGame.scale;
        double xSoFar = 0 - ((strLength / 2) / QuitGame.scale);
        double ySoFar = (splash.chars.size() * (8 * (QuitGame.rotAngle / 90.0))) / 2;

        for(SplashTextCharacter splashTextCharacter : new ArrayList<>(splash.chars)) {
            splashTextCharacter.setTargetPos((float)xSoFar, (float)ySoFar);
            if(absolute) splashTextCharacter.setRenderPos((float)xSoFar, (float)ySoFar);

            xSoFar += getFont().getSplitter().stringWidth(String.valueOf(splashTextCharacter.getChar()));
            ySoFar -= (splashTextCharacter.width * 1.6) * (QuitGame.rotAngle / 90.0);
        }

        for(SplashTextCharacter splashTextCharacter : new ArrayList<>(splash.chars)) {
            if(splashTextCharacter.isDragging()) {
                int idx = quitgame$getMouseCharIndex();
                if(idx != -1) {
                    splash.reorder(splashTextCharacter, idx);
                }
            }
        }
    }

    @Unique
    private int quitgame$getMouseCharIndex() {
        int i = 0;
        for(SplashTextCharacter splashTextCharacter : new ArrayList<>(splash.chars)) {
            if(splashTextCharacter.hoveredXAxis(mouseX)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    @Unique
    private float quitgame$getAlpha() {
        float a = 1.0F;
        if (this.fading) {
            float g = (float)(Util.getMillis() - this.fadeInStart) / 2000.0F;
            if (g > 1.0F) {
                this.fading = false;
            } else {
                g = Mth.clamp(g, 0.0F, 1.0F);
                a = Mth.clampedMap(g, 0.5F, 1.0F, 0.0F, 1.0F);
            }
        }
        return a;
    }
}
