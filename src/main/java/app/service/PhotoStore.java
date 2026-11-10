package app.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/** Copies uploaded photos into the application's photos folder. */
public final class PhotoStore {

    private static final Path DIR = Path.of("photos");
    private static final long MAX_BYTES = 8L * 1024 * 1024;
    private static final Set<String> EXTENSIONS =
            Set.of("jpg", "jpeg", "png", "gif", "bmp");

    private PhotoStore() { }

    /** Saves a copy of the file and returns its path relative to the app. */
    public static String save(File source) {
        String extension = extensionOf(source.getName());
        if (!EXTENSIONS.contains(extension)) {
            throw new ServiceException(
                    "Please choose a JPG, PNG, GIF or BMP image.");
        }
        if (source.length() > MAX_BYTES) {
            throw new ServiceException(
                    "The photo must be smaller than 8 MB.");
        }
        try {
            Files.createDirectories(DIR);
            String name = UUID.randomUUID() + "." + extension;
            Files.copy(source.toPath(), DIR.resolve(name),
                    StandardCopyOption.REPLACE_EXISTING);
            return DIR.getFileName() + "/" + name;
        } catch (IOException e) {
            throw new ServiceException("The photo could not be saved.");
        }
    }

    public static File resolve(String relativePath) {
        return Path.of(relativePath).toFile();
    }

    private static String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? ""
                : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
