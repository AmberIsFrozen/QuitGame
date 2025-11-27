package com.lx862.quitgame;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector2d;
import org.joml.Vector2f;

public class SplashTextCharacter {
    private final char character;
    private Vector2f startPos;
    private Vector2f targetPos;
    private Vector2f renderedPos;
    private boolean dragging;
    public double width;

    public SplashTextCharacter(char character) {
        this.character = character;
        this.targetPos = new Vector2f(0, 0);
        this.renderedPos = new Vector2f(0, 0);
        this.width = MinecraftClient.getInstance().textRenderer.getWidth(String.valueOf(character));
    }

    public char getChar() {
        return character;
    }

    public void setStartPos(float x, float y) {
        this.startPos = new Vector2f(x, y);
    }

    public void setTargetPos(float x, float y) {
        this.targetPos = new Vector2f(x, y);
    }

    public void setRenderPos(float x, float y) {
        this.renderedPos = new Vector2f(x, y);
    }

    public boolean hovered(double mouseX, double mouseY) {
        return hoveredXAxis(mouseX) && hoveredYAxis(mouseY);
    }

    public boolean hoveredXAxis(double mouseX) {
        double startX = (startPos.x) + (targetPos.x * QuitGame.scale);
        double endX = startX + ((width + 0.5) * QuitGame.scale);
        return mouseX >= startX && mouseX <= endX;
    }

    public boolean hoveredYAxis(double mouseY) {
        double startY = (startPos.y) + ((targetPos.y - 1) * QuitGame.scale);
        double endY = startY + ((8 + 2) * QuitGame.scale);
        return mouseY >= startY && mouseY <= endY;
    }

    public void render(DrawContext drawContext, double deltaTime, float alpha, TextRenderer textRenderer) {
        renderedPos = renderedPos.lerp(targetPos, (float)deltaTime);

        drawContext.getMatrices().pushMatrix();
        drawContext.getMatrices().translate(renderedPos.x, renderedPos.y);
        drawContext.getMatrices().rotate((float)(-QuitGame.rotAngle * Math.PI) / 180f);
        drawContext.drawTextWithShadow(textRenderer, String.valueOf(character), 0, 0, ColorHelper.withAlpha(alpha, 16776960));
        drawContext.getMatrices().popMatrix();
    }

    public void renderBoundary(DrawContext drawContext, double deltaTime, TextRenderer textRenderer) {
        double startX = (startPos.x) + (targetPos.x * QuitGame.scale);
        double startY = (startPos.y) + ((targetPos.y - 1) * QuitGame.scale);
        double endX = ((width + 0.5) * QuitGame.scale);
        double endY = ((8 + 2) * QuitGame.scale);

        drawContext.drawStrokedRectangle((int)startX, (int)startY, (int)endX, (int)endY, 0xFFFFFFFF);
    }

    public void dragged() {
        dragging = true;
    }

    public boolean isDragging() {
        return dragging;
    }

    public void released() {
        dragging = false;
    }
}
