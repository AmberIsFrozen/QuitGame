package com.lx862.quitgame.mixin;

import com.lx862.quitgame.ReorderableSplashText;
import com.lx862.quitgame.SplashTextCharacter;
import com.lx862.quitgame.QuitGame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SplashTextRenderer;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.PressableTextWidget;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
    @Shadow private boolean doBackgroundFade;
    @Shadow private long backgroundFadeStart;
    @Unique
    private static ReorderableSplashText splash;
    @Unique
    private double mouseX;
    @Unique
    public float startX = 0;
    @Unique
    public float startY = 0;
    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        if(splash == null) {
            SplashTextRenderer splashTextRenderer = MinecraftClient.getInstance().getSplashTextLoader().get();
            if(splashTextRenderer != null) {
                splash = new ReorderableSplashText(((SplashTextRendererAccessorMixin)splashTextRenderer).getText());
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
    public void hideWidget(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        for(String keyword : QuitGame.keywords) {
            if(splash.startsWith(keyword)) {
                QuitGame.unlocked = true;
            }
        }

        if(QuitGame.unlocked) {
            QuitGame.unlockedAlpha = MathHelper.clamp(QuitGame.unlockedAlpha + (delta / 20F), 0.0F, 1.0F);
        }

        for(Element element : children()) {
            if(element instanceof ButtonWidget) {
                TextContent msg = ((ButtonWidget)element).getMessage().getContent();
                if(element instanceof PressableTextWidget) continue; // Copyright
                if(msg instanceof TranslatableTextContent) {
                    if(((TranslatableTextContent) msg).getKey().contains("quit")) {
                        continue;
                    }
                }

                ((ButtonWidget) element).visible = QuitGame.unlocked;
                ((ButtonWidget) element).setAlpha(QuitGame.unlockedAlpha);
            }
        }
    }


    @Inject(method = "render", at = @At("TAIL"))
    public void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        QuitGame.scale = 1.8F * 100.0F / (float)(textRenderer.getWidth(splash.text) + 32);

        float scale = (float)QuitGame.scale - MathHelper.abs(MathHelper.sin((float)(Util.getMeasuringTimeMs() % 1000L) / 1000.0F * 6.2831855F) * (0.1F * ((float)QuitGame.scale / 1.8F)));
        quitgame$positionCharacters(false);

        context.getMatrices().pushMatrix();
        context.getMatrices().translate(startX, startY);
        context.getMatrices().scale(scale, scale);
        for(SplashTextCharacter splashTextCharacter : new ArrayList<>(splash.chars)) {
            context.getMatrices().pushMatrix();
            splashTextCharacter.render(context, delta / 3f, quitgame$getAlpha(), textRenderer);
            context.getMatrices().popMatrix();
        }

        context.getMatrices().popMatrix();

//        for(CharacterRenderer characterRenderer : new ArrayList<>(QuitGame.splash.chars)) {
//            characterRenderer.renderBoundary(context, delta / 3f, textRenderer);
//        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        for(SplashTextCharacter splashTextCharacter : splash.chars) {
            if(splashTextCharacter.hovered(click.x(), click.y())) {
                splashTextCharacter.dragged();
                return super.mouseClicked(click, doubled);
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(Click click) {
        for(SplashTextCharacter splashTextCharacter : splash.chars) {
            splashTextCharacter.released();
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(Click click, double mouseX, double mouseY) {
        this.mouseX = click.x();
        return super.mouseDragged(click, mouseX, mouseY);
    }

    @Unique
    public void quitgame$positionCharacters(boolean absolute) {
        QuitGame.scale = 1.75F * (100.0F / (float)(textRenderer.getWidth(splash.text) + 32));

        double strLength = textRenderer.getWidth(splash.text) * QuitGame.scale;
        double xSoFar = 0 - ((strLength / 2) / QuitGame.scale);
        double ySoFar = (splash.chars.size() * (8 * (QuitGame.rotAngle / 90.0))) / 2;

        for(SplashTextCharacter splashTextCharacter : new ArrayList<>(splash.chars)) {
            splashTextCharacter.setTargetPos((float)xSoFar, (float)ySoFar);
            if(absolute) splashTextCharacter.setRenderPos((float)xSoFar, (float)ySoFar);

            xSoFar += textRenderer.getTextHandler().getWidth(String.valueOf(splashTextCharacter.getChar()));
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
        if (this.doBackgroundFade) {
            float g = (float)(Util.getMeasuringTimeMs() - this.backgroundFadeStart) / 2000.0F;
            if (g > 1.0F) {
                this.doBackgroundFade = false;
            } else {
                g = MathHelper.clamp(g, 0.0F, 1.0F);
                a = MathHelper.clampedMap(g, 0.5F, 1.0F, 0.0F, 1.0F);
            }
        }
        return a;
    }
}
