package com.artur_sres.multinova.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import javax.imageio.ImageIO;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.UUID;

@Service
public class ImageStorageService {
    private final Path root;
    public ImageStorageService(@Value("${app.images.directory:uploads}") String directory) {
        root = Path.of(directory).toAbsolutePath().normalize();
    }

    public String store(MultipartFile file) {
        if (file.isEmpty() || file.getSize() > 5 * 1024 * 1024)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione uma imagem de até 5 MB.");
        BufferedImage source;
        try (var input = ImageIO.createImageInputStream(new ByteArrayInputStream(file.getBytes()))) {
            var readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new IOException();
            var reader = readers.next();
            try {
                reader.setInput(input);
                String format = reader.getFormatName();
                if (!(format.equalsIgnoreCase("JPEG") || format.equalsIgnoreCase("PNG"))) throw new IOException();
                int width = reader.getWidth(0), height = reader.getHeight(0);
                if (width <= 0 || height <= 0 || (long) width * height > 20_000_000) throw new IOException();
                source = reader.read(0);
            } finally { reader.dispose(); }
        } catch (IOException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Imagem inválida. Use JPEG ou PNG com até 20 megapixels.");
        }
        double scale = Math.min(1.0, 1200.0 / Math.max(source.getWidth(), source.getHeight()));
        var resized = new BufferedImage(Math.max(1, (int) (source.getWidth() * scale)),
                Math.max(1, (int) (source.getHeight() * scale)), BufferedImage.TYPE_INT_ARGB);
        var graphics = resized.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.drawImage(source, 0, 0, resized.getWidth(), resized.getHeight(), null);
        } finally { graphics.dispose(); }
        String key = UUID.randomUUID() + ".png";
        try {
            Files.createDirectories(root);
            try (var output = Files.newOutputStream(path(key), StandardOpenOption.CREATE_NEW)) {
                ImageIO.write(resized, "png", output);
            }
            return key;
        } catch (IOException e) {
            delete(key);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível salvar a imagem.");
        }
    }

    private Path path(String key) {
        if (key == null || !key.matches("[a-f0-9-]{36}\\.png"))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Imagem não encontrada.");
        return root.resolve(key);
    }

    public byte[] read(String key) {
        try { return Files.readAllBytes(path(key)); }
        catch (IOException e) { throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Imagem não encontrada."); }
    }

    public void delete(String key) {
        if (key == null) return;
        try { Files.deleteIfExists(path(key)); }
        catch (IOException e) {
            org.slf4j.LoggerFactory.getLogger(getClass()).warn("Não foi possível remover a imagem {}", key, e);
        }
    }
}
