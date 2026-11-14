package app.ui;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;

/** Circular score indicator that can count up from 0 to the score. */
public class ScoreRing extends StackPane {

    private final Arc arc;
    private final Label label = new Label("0%");
    private final DoubleProperty progress = new SimpleDoubleProperty(0);
    private double target;

    public ScoreRing(double size, double strokeWidth) {
        double center = size / 2;
        double radius = (size - strokeWidth) / 2;

        Circle track = new Circle(center, center, radius);
        track.setFill(Color.TRANSPARENT);
        track.setStroke(Color.rgb(128, 128, 128, 0.25));
        track.setStrokeWidth(strokeWidth);

        arc = new Arc(center, center, radius, radius, 90, 0);
        arc.setType(ArcType.OPEN);
        arc.setFill(Color.TRANSPARENT);
        arc.setStrokeWidth(strokeWidth);
        arc.setStrokeLineCap(StrokeLineCap.ROUND);

        // a Pane keeps the shapes where they are while the arc grows
        Pane canvas = new Pane(track, arc);
        canvas.setPrefSize(size, size);
        canvas.setMinSize(size, size);
        canvas.setMaxSize(size, size);

        label.getStyleClass().add("ring-label");
        label.setStyle("-fx-font-size: " + Math.round(size / 4.2) + "px;");

        setPrefSize(size, size);
        setMinSize(size, size);
        setMaxSize(size, size);
        getChildren().addAll(canvas, label);

        progress.addListener((obs, old, now) -> draw(now.doubleValue()));
    }

    /** Shows the score at once, without animation. */
    public void setScore(double score) {
        target = clamp(score);
        progress.set(target);
        draw(target);
    }

    /** Counts up from 0 to the score. */
    public void play(double score) {
        target = clamp(score);
        progress.set(0);
        draw(0);
        new Timeline(new KeyFrame(Duration.millis(1200),
                new KeyValue(progress, target,
                        Interpolator.EASE_OUT))).play();
    }

    private void draw(double value) {
        arc.setLength(-360 * value);
        arc.setStroke(colorFor(target));
        label.setText(Math.round(value * 100) + "%");
    }

    private static Color colorFor(double score) {
        if (score >= 0.70) {
            return Color.web("#059669");
        }
        return score >= 0.55 ? Color.web("#d97706") : Color.web("#ea580c");
    }

    private static double clamp(double score) {
        return Math.max(0, Math.min(1, score));
    }
}
