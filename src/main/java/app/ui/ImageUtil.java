package app.ui;

import app.model.Report;
import app.service.PhotoStore;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

import java.io.File;

final class ImageUtil {

    private ImageUtil() { }

    /** Loads a stored photo, or returns null if there is none. */
    static Image load(String relativePath, double requestedWidth) {
        if (relativePath == null || relativePath.isBlank()) {
            return null;
        }
        File file = PhotoStore.resolve(relativePath);
        if (!file.isFile()) {
            return null;
        }
        Image image = new Image(file.toURI().toString(), requestedWidth, 0,
                true, true, false);
        return image.isError() || image.getHeight() == 0 ? null : image;
    }

    /** A rounded box showing the photo cropped to fill, or a placeholder. */
    static StackPane photoBox(Report report, double width, double height) {
        StackPane box = new StackPane();
        box.setPrefSize(width, height);
        box.setMinSize(width, height);
        box.setMaxSize(width, height);

        Rectangle clip = new Rectangle(width, height);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        box.setClip(clip);

        Image image = load(report.photoPath(), width * 2);
        if (image != null) {
            ImageView view = new ImageView(image);
            double target = width / height;
            double ratio = image.getWidth() / image.getHeight();
            Rectangle2D viewport;
            if (ratio > target) {
                double w = image.getHeight() * target;
                viewport = new Rectangle2D((image.getWidth() - w) / 2, 0,
                        w, image.getHeight());
            } else {
                double h = image.getWidth() / target;
                viewport = new Rectangle2D(0, (image.getHeight() - h) / 2,
                        image.getWidth(), h);
            }
            view.setViewport(viewport);
            view.setFitWidth(width);
            view.setFitHeight(height);
            view.setPreserveRatio(false);
            view.setSmooth(true);
            box.getChildren().add(view);
        } else {
            box.getStyleClass().add("photo-placeholder");
            Label initial = new Label(
                    report.category().substring(0, 1).toUpperCase());
            initial.getStyleClass().add("placeholder-initial");
            box.getChildren().add(initial);
        }
        return box;
    }
}
