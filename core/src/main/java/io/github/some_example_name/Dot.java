package io.github.some_example_name;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
//import com.badlogic.gdx.utils.Array;

public class Dot {
    private Vector2 position;
    private boolean isOrigin;
    private static final float BASE_RADIUS = .04f;
    private static final float HOVER_RADIUS = .06f;
    // private Array<Line> lines = new Array<>();
    // private boolean lineMode = false;

    public Dot(Vector2 position) {
        this(position, false);
    }

    public Dot(Vector2 position, boolean isOrigin) {
        this.position = position;
        this.isOrigin = isOrigin;
    }

    public boolean isHovered(Vector2 mousePos) {
        return position.dst(mousePos) < HOVER_RADIUS;
    }

    public void setPosition(Vector2 newPosition) {
        this.position.set(newPosition);
    }

    public Vector2 getPosition() {
        return position;
    }

    public boolean isOrigin() {
        return isOrigin;
    }

    public void render(ShapeRenderer renderer, Vector2 mousePos, boolean selected, float zoom) {
        Color colorToUse = isOrigin ? Color.YELLOW : (selected ? Color.GREEN : Color.PURPLE);
        renderer.setColor(colorToUse);

        float baseRadius = selected ? HOVER_RADIUS : BASE_RADIUS;
        float adjustedRadius = baseRadius * zoom;

        renderer.circle(position.x, position.y, adjustedRadius, 60); // 60 segments = smoother
    }
}
