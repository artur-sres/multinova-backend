package com.artur_sres.multinova.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

class ImageStorageServiceTests {
    @TempDir Path directory;

    @Test void normalizesResizesAndPersistsAcrossServiceInstances() throws Exception {
        var bytes = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(2400, 1200, BufferedImage.TYPE_INT_RGB), "jpg", bytes);
        var storage = new ImageStorageService(directory.toString());
        String key = storage.store(new MockMultipartFile("file", "../../photo.jpg", "image/jpeg", bytes.toByteArray()));
        assertTrue(Files.exists(directory.resolve(key)));
        var reopened = new ImageStorageService(directory.toString());
        var image = ImageIO.read(new ByteArrayInputStream(reopened.read(key)));
        assertEquals(1200, image.getWidth());
        assertEquals(600, image.getHeight());
        storage.delete(key);
        assertThrows(ResponseStatusException.class, () -> storage.read(key));
    }

    @Test void rejectsFakeEmptyOversizedAndTraversalInputs() {
        var storage = new ImageStorageService(directory.toString());
        assertThrows(ResponseStatusException.class, () -> storage.store(new MockMultipartFile("file", "x.png", "image/png", "not an image".getBytes())));
        assertThrows(ResponseStatusException.class, () -> storage.store(new MockMultipartFile("file", new byte[0])));
        assertThrows(ResponseStatusException.class, () -> storage.store(new MockMultipartFile("file", new byte[5 * 1024 * 1024 + 1])));
        assertThrows(ResponseStatusException.class, () -> storage.read("../secret.png"));
        assertThrows(ResponseStatusException.class, () -> storage.read(null));
    }
}
