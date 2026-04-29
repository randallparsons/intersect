package io.github.some_example_name;

//import io.github.some_example_name.Dot;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
//import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import com.badlogic.gdx.math.MathUtils;

public class Main extends ApplicationAdapter {
	private ShapeRenderer shapeRenderer;
	private OrthographicCamera camera;

	// private static final float GRID_SIZE = 50f;
	private static final float GRID_SIZE = .5f;
	private Vector2 gridOrigin;

	private Array<Dot> dots;
	private Dot selectedDot = null;
	private Stage stage;
	private TextButton moveButton, deleteButton;
	private boolean moveMode = false;
	private SpriteBatch batch;
	private Dot originDot;

	private boolean lineMode;

	private Array<Line> lines = new Array<>();
	private TextButton lineButton;

	@Override
	public void create() {
		shapeRenderer = new ShapeRenderer();
		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.position.set(0, 0, 0);
		camera.update();
		camera.zoom = 1f; // Normal size — increase for zoom out, decrease for zoom in

		// Initialize dots list
		dots = new Array<>();

		// Snap grid origin to nearest intersection from (0,0)
		float snappedX = Math.round(0 / GRID_SIZE) * GRID_SIZE;
		float snappedY = Math.round(0 / GRID_SIZE) * GRID_SIZE;
		gridOrigin = new Vector2(snappedX, snappedY);

		// Place the origin dot at the grid origin
		originDot = new Dot(new Vector2(gridOrigin), true);

		// UI and input setup
		batch = new SpriteBatch();
		stage = new Stage(new ScreenViewport());

		BitmapFont font = new BitmapFont();
		TextButtonStyle buttonStyle = new TextButtonStyle();
		buttonStyle.font = font;
		buttonStyle.fontColor = Color.WHITE;

		moveButton = new TextButton("Move", buttonStyle);
		deleteButton = new TextButton("Delete", buttonStyle);
		lineButton = new TextButton("Line", buttonStyle); // ✅ sets the class field
		// TextButton lineButton = new TextButton("Line", buttonStyle);

		// Set sizes
		moveButton.setSize(80, 40);
		deleteButton.setSize(80, 40);
		lineButton.setSize(80, 40);

		// Set positions (stacked vertically, right side of screen)
		moveButton.setPosition(Gdx.graphics.getWidth() - 100, Gdx.graphics.getHeight() - 50);
		deleteButton.setPosition(Gdx.graphics.getWidth() - 100, Gdx.graphics.getHeight() - 100);
		lineButton.setPosition(Gdx.graphics.getWidth() - 100, Gdx.graphics.getHeight() - 150);

		// Move button behavior
		moveButton.addListener(new ClickListener() {
			@Override
			public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
				if (selectedDot != null) {
					moveMode = !moveMode;
					lineMode = false;
					moveButton.getLabel().setColor(moveMode ? Color.GREEN : Color.WHITE);
				}
			}
		});

		// Delete button behavior
		deleteButton.addListener(new ClickListener() {
			@Override
			public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
				if (selectedDot != null) {
					dots.removeValue(selectedDot, true);

					// 🔽 Add this block to remove connected lines
					Array<Line> linesToRemove = new Array<>();
					for (Line line : lines) {
						if (line.start == selectedDot || line.end == selectedDot) {
							linesToRemove.add(line);
						}
					}
					for (Line line : linesToRemove) {
						lines.removeValue(line, true);
					}

					selectedDot = null;
					moveMode = false;
					lineMode = false;
					moveButton.getLabel().setColor(Color.WHITE);
					updateButtonVisibility();
				}
			}
		});

		lineButton.setSize(80, 40);
		lineButton.setPosition(Gdx.graphics.getWidth() - 100, Gdx.graphics.getHeight() - 150);

		lineButton.addListener(new ClickListener() {
			@Override
			public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
				if (selectedDot != null) {
					lineMode = !lineMode;
					lineButton.getLabel().setColor(lineMode ? Color.GREEN : Color.WHITE);
				}
			}
		});

		stage.addActor(lineButton);
		lineButton.setVisible(false); // Starts hidden, becomes visible when dot is selected

		// Add to stage
		stage.addActor(moveButton);
		stage.addActor(deleteButton);
		stage.addActor(lineButton);

		// Hide by default
		moveButton.setVisible(false);
		deleteButton.setVisible(false);
		lineButton.setVisible(false);

		/*
		 * stage.addActor(moveButton);
		 * stage.addActor(deleteButton);
		 * moveButton.setVisible(false);
		 * deleteButton.setVisible(false);
		 */

		InputMultiplexer inputMultiplexer = new InputMultiplexer();
		inputMultiplexer.addProcessor(stage);
		inputMultiplexer.addProcessor(new InputAdapter() {
			@Override
			public boolean scrolled(float amountX, float amountY) {
				float zoomFactor = 0.1f; // how much zoom changes per scroll step

				// Scroll up = zoom in, scroll down = zoom out
				camera.zoom += amountY * zoomFactor;

				// Clamp to reasonable limits
				camera.zoom = MathUtils.clamp(camera.zoom, 0.1f, 100f);

				return true;
			}

			@Override
			public boolean touchDown(int screenX, int screenY, int pointer, int button) {
				if (moveMode && selectedDot != null) {
					return true; // Prevent deselecting while moving
				}

				if (lineMode && selectedDot != null) {
					Vector2 worldPos = screenToWorld(screenX, screenY);

					// Create new dot
					Dot newDot = new Dot(worldPos);
					dots.add(newDot);

					// Create line from selectedDot to newDot
					lines.add(new Line(selectedDot, newDot));

					// Make new dot the selected one
					selectedDot = newDot;

					// Keep line mode active or turn off here if you prefer
					updateButtonVisibility();
					return true;
				}

				Vector2 worldPos = screenToWorld(screenX, screenY);
				for (Dot dot : dots) {
					if (dot.isHovered(worldPos)) {
						if (dot == selectedDot) {
							selectedDot = null;
							moveMode = false;
							moveButton.getLabel().setColor(Color.WHITE);
						} else {
							selectedDot = dot;
						}
						updateButtonVisibility();
						return true;
					}
				}
				dots.add(new Dot(worldPos));
				return true;
			}

			@Override
			public boolean touchDragged(int screenX, int screenY, int pointer) {
				if (moveMode && selectedDot != null) {
					selectedDot.setPosition(screenToWorld(screenX, screenY));
				}
				return true;
			}
		});

		Gdx.input.setInputProcessor(inputMultiplexer);
	}

	public void drawGrid(ShapeRenderer renderer, OrthographicCamera camera) {
		renderer.setProjectionMatrix(camera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Line);
		renderer.setColor(Color.GRAY);

		float left = camera.position.x - camera.viewportWidth / 2 * camera.zoom;
		float right = camera.position.x + camera.viewportWidth / 2 * camera.zoom;
		float bottom = camera.position.y - camera.viewportHeight / 2 * camera.zoom;
		float top = camera.position.y + camera.viewportHeight / 2 * camera.zoom;

		float startX = gridOrigin.x + (float) Math.floor((left - gridOrigin.x) / GRID_SIZE) * GRID_SIZE;
		float startY = gridOrigin.y + (float) Math.floor((bottom - gridOrigin.y) / GRID_SIZE) * GRID_SIZE;

		for (float x = startX; x < right; x += GRID_SIZE) {
			renderer.line(x, bottom, x, top);
		}

		for (float y = startY; y < top; y += GRID_SIZE) {
			renderer.line(left, y, right, y);
		}

		renderer.end();
	}

	private void updateButtonVisibility() {
		boolean visible = selectedDot != null;
		moveButton.setVisible(visible);
		deleteButton.setVisible(visible);
		lineButton.setVisible(selectedDot != null);
	}

	private void updateCamera() {
		float viewportWidth = Gdx.graphics.getWidth() / 100f;
		float viewportHeight = Gdx.graphics.getHeight() / 100f;

		camera.setToOrtho(false, viewportWidth, viewportHeight);
		camera.position.set(0, 0, 0); // Center camera at (0, 0)
		camera.update();
	}

	private Vector2 screenToWorld(int screenX, int screenY) {
		Vector3 screenCoords = new Vector3(screenX, screenY, 0);
		camera.unproject(screenCoords);
		return new Vector2(screenCoords.x, screenCoords.y);
	}

	@Override
	public void render() {
		updateCamera();
		ScreenUtils.clear(0f, 0f, 0f, 1f);

		camera.update();

		// Draw the grid using the anchored origin
		drawGrid(shapeRenderer, camera);

		// Get current mouse world position
		Vector2 mousePos = screenToWorld(Gdx.input.getX(), Gdx.input.getY());

		// Draw lines first
		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		for (Line line : lines) {
			line.render(shapeRenderer);
		}
		shapeRenderer.end();

		// Draw lines first (optional, comment out if you’re not ready for lines yet)
		// shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		// for (Line line : lines) {
		// line.render(shapeRenderer);
		// }
		// shapeRenderer.end();

		// Draw dots, including origin
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

		// Draw origin dot (always static and not hoverable)
		originDot.render(shapeRenderer, mousePos, false, camera.zoom);

		// Draw other dots
		for (Dot dot : dots) {
			boolean isHovered = dot.isHovered(mousePos);
			boolean isSelected = dot == selectedDot;
			dot.render(shapeRenderer, mousePos, isHovered || isSelected, camera.zoom);
		}

		shapeRenderer.end();

		// Draw UI stage (Move/Delete buttons)
		stage.act();
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
		updateCamera();
		stage.getViewport().update(width, height, true);

		moveButton.setPosition(width - 100, height - 50);
		deleteButton.setPosition(width - 100, height - 100);
		lineButton.setPosition(width - 100, height - 150);
	}

	@Override
	public void dispose() {
		shapeRenderer.dispose();
		stage.dispose();
		batch.dispose();
	}
}
