package io.github.some_example_name;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
//import com.badlogic.gdx.math.Vector2;

/**
 * Represents a line segment connecting two dots.
 */
public class Line {
    public Dot start;
    public Dot end;

    /**
     * Constructs a line between two dots.
     *
     * @param start the starting dot
     * @param end   the ending dot
     */
    public Line(Dot start, Dot end) {
        this.start = start;
        this.end = end;
    }

    /**
     * Renders the line as a cyan line segment using the provided renderer.
     *
     * @param renderer the shape renderer to use for drawing
     */
    public void render(ShapeRenderer renderer) {
        renderer.setColor(Color.CYAN);
        renderer.line(start.getPosition(), end.getPosition());
    }

    /**
     * Checks if the given dot is either the start or end point of this line.
     *
     * @param dot the dot to check
     * @return true if the dot is an endpoint of this line, false otherwise
     */
    public boolean containsDot(Dot dot) {
        return dot == start || dot == end;
    }

    /**
     * Returns the starting dot of this line.
     *
     * @return the start dot
     */
    public Dot getStart() {
        return start;
    }

    /**
     * Returns the ending dot of this line.
     *
     * @return the end dot
     */
    public Dot getEnd() {
        return end;
    }
}
